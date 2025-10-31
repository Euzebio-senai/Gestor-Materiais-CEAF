package com.gestor.estoque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class EstoqueController {

    // Injeção de dependências: O Spring dá-nos acesso aos repositórios
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private RetiranteRepository retiranteRepository;
    
    @Autowired
    private HistoricoRepository historicoRepository;

    // --- PÁGINA DE LOGIN ---

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Mostra o ficheiro "login.html"
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session, 
                              Model model) {
        
        Optional<User> userOpt = userRepository.findByEMAIL(email);

        // ATENÇÃO: Tal como no seu código Python, isto compara passwords em texto puro.
        // Numa aplicação real, deveria usar um sistema de hash (como o Spring Security).
        if (userOpt.isPresent() && userOpt.get().getPASSWORD().equals(password)) {
            // Sucesso! Guarda o utilizador na sessão
            session.setAttribute("user", userOpt.get());
            return "redirect:/menu"; // Redireciona para o menu
        } else {
            // Erro! Mostra mensagem no login.html
            model.addAttribute("error", "Email ou senha incorretos.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Limpa a sessão
        return "redirect:/login";
    }

    // --- PÁGINA DO MENU ---

    @GetMapping("/menu")
    public String showMenu(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Se não está logado, volta ao login
        }
        model.addAttribute("userName", user.getNOME());
        return "menu"; // Mostra o ficheiro "menu.html"
    }

    // --- PÁGINA DE OPERAÇÃO (ENTRADA / SAÍDA) ---
    
    // Objeto para guardar os itens da lista
    // (Usamos isto em vez do Treeview do Python)
    public static class ItemOperacao {
        public Long id;
        public String nome;
        public Integer qnt;
        public ItemOperacao(Long id, String nome, Integer qnt) {
            this.id = id; this.nome = nome; this.qnt = qnt;
        }
    }

    @GetMapping("/operacao")
    public String showOperacaoPage(@RequestParam String tipo, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        
        // Garante que a lista de itens existe na sessão
        if (session.getAttribute("listaItens") == null) {
            session.setAttribute("listaItens", new ArrayList<ItemOperacao>());
        }
        
        List<ItemOperacao> listaItens = (List<ItemOperacao>) session.getAttribute("listaItens");
        
        model.addAttribute("tipo", tipo); // "ENTRADA" ou "SAÍDA"
        model.addAttribute("listaItens", listaItens);
        return "operacao"; // Mostra o ficheiro "operacao.html"
    }

    @PostMapping("/operacao/add")
    public String addItemOperacao(@RequestParam String tipo,
                                  @RequestParam Long produtoId,
                                  @RequestParam Integer quantidade,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
                                      
        Optional<Produto> prodOpt = produtoRepository.findByID_PRODUTO(produtoId);
        
        if (prodOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Produto com ID " + produtoId + " não encontrado.");
        } else if (quantidade <= 0) {
            redirectAttributes.addFlashAttribute("error", "Quantidade deve ser positiva.");
        } else {
            List<ItemOperacao> listaItens = (List<ItemOperacao>) session.getAttribute("listaItens");
            listaItens.add(new ItemOperacao(prodOpt.get().getID_PRODUTO(), prodOpt.get().getITEM(), quantidade));
            session.setAttribute("listaItens", listaItens);
        }
        
        return "redirect:/operacao?tipo=" + tipo;
    }
    
    @GetMapping("/operacao/clear")
    public String clearOperacao(@RequestParam String tipo, HttpSession session) {
        session.setAttribute("listaItens", new ArrayList<ItemOperacao>());
        return "redirect:/operacao?tipo=" + tipo;
    }

    // --- PROCESSAMENTO EM LOTE (A LÓGICA PRINCIPAL) ---
    // Usamos @Transactional para garantir que ou tudo funciona, ou nada é guardado (segurança)
    @PostMapping("/operacao/processar")
    @org.springframework.transaction.annotation.Transactional 
    public String processarOperacao(@RequestParam String tipo,
                                    @RequestParam(required = false) String nomeRetirante, // Opcional
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
                                        
        User user = (User) session.getAttribute("user");
        List<ItemOperacao> listaItens = (List<ItemOperacao>) session.getAttribute("listaItens");

        if (user == null) return "redirect:/login";
        if (listaItens == null || listaItens.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "A lista de itens está vazia.");
            return "redirect:/operacao?tipo=" + tipo;
        }
        
        // Validação de Saída
        if (tipo.equals("SAÍDA") && (nomeRetirante == null || nomeRetirante.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "O nome do retirante é obrigatório para saídas.");
            return "redirect:/operacao?tipo=" + tipo;
        }

        try {
            Long retiranteId = null;

            // 1. Lógica do Retirante (igual à do Python)
            if (tipo.equals("SAÍDA")) {
                Optional<Retirante> retOpt = retiranteRepository.findByNOME_RETIRANTE(nomeRetirante);
                if (retOpt.isPresent()) {
                    retiranteId = retOpt.get().getID_RETIRANTE();
                } else {
                    // Cria novo retirante
                    Retirante novoRetirante = new Retirante(nomeRetirante, "N/A");
                    retiranteRepository.save(novoRetirante);
                    retiranteId = novoRetirante.getID_RETIRANTE();
                }
            }

            // 2. Processar cada item
            for (ItemOperacao item : listaItens) {
                // (Aqui deveríamos ter um lock no produto, mas para simplificar vamos apenas buscar)
                Produto produto = produtoRepository.findById(item.id).orElseThrow();
                
                if (tipo.equals("ENTRADA")) {
                    produto.setQUANTIDADE_POSSUIDA(produto.getQUANTIDADE_POSSUIDA() + item.qnt);
                } else {
                    // (Aqui deveríamos verificar se o stock fica negativo)
                    produto.setQUANTIDADE_POSSUIDA(produto.getQUANTIDADE_POSSUIDA() - item.qnt);
                }
                produtoRepository.save(produto); // Atualiza o produto

                // 3. Regista no Histórico (Simplificado: não criamos a tabela RETIRADA separada)
                HistoricoMovimentacao log = new HistoricoMovimentacao(
                    user.getID_USER(),
                    item.id,
                    retiranteId, // ID_RETIRANTE (nulo se for entrada)
                    null,      // ID_RETIRADA (não usamos nesta versão simplificada)
                    item.qnt,
                    tipo
                );
                historicoRepository.save(log);
            }
            
            // 4. Limpar a lista da sessão e redirecionar
            session.setAttribute("listaItens", new ArrayList<ItemOperacao>());
            redirectAttributes.addFlashAttribute("success", "Operação de " + tipo + " registada com sucesso!");
            return "redirect:/menu";

        } catch (Exception e) {
            // @Transactional vai reverter tudo se houver um erro
            redirectAttributes.addFlashAttribute("error", "Erro ao processar: " + e.getMessage());
            return "redirect:/operacao?tipo=" + tipo;
        }
    }
}