(ns curso6.sample
  (:require [curso6.db.categoria :as db.categoria]
            [curso6.db.produto :as db.produto]
            [curso6.model :as model]
            [curso6.db.venda :as db.venda]))


(defn adiciona-dados [conn]
  (let [eletronicos    (model/nova-categoria "Eletrônicos")
        esporte        (model/nova-categoria "Esporte")
        computador     (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M, 10)
        celular        (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M)
        celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)
        xadrez         (model/novo-produto (model/uuid) "Tabuleiro de xadrez", "tabuleiro-de-xadrez", 30M, 5)
        jogo-online    (assoc (model/novo-produto (model/uuid) "Jogo online", "/jogo-online", 20M) :produto/digital true)]
    @(db.categoria/add conn [eletronicos, esporte])
    @(db.produto/add conn [computador, celular, celular-barato, xadrez, jogo-online] "200.216.222.125")
    (db.produto/add-category conn [computador, celular, celular-barato, jogo-online] eletronicos)
    (db.produto/add-category conn [xadrez] esporte)
    (db.venda/add conn (:produto/id computador) 3)
    (db.venda/add conn (:produto/id computador) 4)
    (db.venda/add conn (:produto/id computador) 8)))
