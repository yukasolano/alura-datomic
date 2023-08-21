(ns curso2.aula6
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso2.db :as db]
            [curso2.model :as model]))

; Curso 2 - Datomic Queries: avançando com o modelo e pesquisas
; Aula 6 - Nested queries e transações

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Create categories
(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))
@(db/adiciona-categorias conn [eletronicos, esporte])

;Create products
(def computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M))
(def celular (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
(def xadrez (model/novo-produto "Tabuleiro de xadrez", "tabuleiro-de-xadrez", 30M))
@(db/adiciona-produtos conn [computador, celular, calculadora, celular-barato, xadrez] "200.216.222.125")

;Associate categories
(db/atribui-categorias conn [computador, celular, celular-barato] eletronicos)
(db/atribui-categorias conn [xadrez] esporte)

;Add more products
@(db/adiciona-produtos conn [{:produto/nome      "Camiseta"
                              :produto/slug      "/camiseta"
                              :produto/preco     30M
                              :produto/id        (model/uuid)
                              :produto/categoria {:categoria/nome "Roupas"
                                                  :categoria/id   (model/uuid)}}] "200.216.222.125")
@(db/adiciona-produtos conn [{:produto/nome      "Dama"
                              :produto/slug      "/dama"
                              :produto/preco     15M
                              :produto/id        (model/uuid)
                              :produto/categoria [:categoria/id (:categoria/id esporte)]}])

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

(println "All products with their categories")
(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))
(println "Todos os produtos mais caros")
(pprint (db/todos-os-produtos-mais-caros (d/db conn)))
(println "Todos os produtos mais baratos")
(pprint (db/todos-os-produtos-mais-baratos (d/db conn)))
(println "Todos os produtos do ip 200.216.222.125")
(pprint (db/todos-os-produtos-do-ip (d/db conn) "200.216.222.125"))
(println "Todos os produtos de um ip que nao existe")
(pprint (db/todos-os-produtos-do-ip (d/db conn) "20.216.222.125"))

; Delete db
(db/apaga-banco)

