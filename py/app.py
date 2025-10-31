from flask import Flask, render_template, request

app = Flask(__name__)

# Documentação: Rota 1 (Página Principal)
# Esta função 'index' corre quando alguém visita a raiz do site ('/')
@app.route('/')
def index():
    dados = {
        'titulo': 'Página Inicial'
    }
    # Ela renderiza o template 'pagina.html'
    return render_template('pagina.html', **dados)


# Documentação: Rota 2 (Página de Operação)
# Esta função 'operacao' corre quando alguém visita '/operacao'
# (o que acontece quando clicas nos links)
@app.route('/operacao')
def operacao():
    
    # Obtém o parâmetro 'tipo' do URL (ex: ?tipo=entrada)
    tipo_de_operacao = request.args.get('tipo') 

    if tipo_de_operacao == 'saida':
        mensagem = "Você está a fazer uma SAÍDA."
    elif tipo_de_operacao == 'entrada':
        mensagem = "Você está a fazer uma ENTRADA."
    else:
        mensagem = "Tipo de operação desconhecido."

    # Tenta renderizar o template 'operacao.html'
    # O erro 500 acontece se este ficheiro não existir na pasta 'templates'
    return render_template('operacao.html', mensagem_da_operacao=mensagem)


if __name__ == '__main__':
    app.run(debug=True)