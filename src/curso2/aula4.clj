(ns curso2.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso2.db :as db]
            [curso2.model :as model]))

; Curso 2 - Datomic Queries: avançando com o modelo e pesquisas
; Aula 4 - Forward e backward navigation

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
@(db/adiciona-produtos conn [computador, celular, calculadora, celular-barato, xadrez])

;Associate categories
(db/atribui-categorias conn [computador, celular, celular-barato] eletronicos)
(db/atribui-categorias conn [xadrez] esporte)

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

(println "Todos os produtos com suas categorias")
(pprint (db/todos-os-nomes-de-produtos-e-categorias (d/db conn)))
(println "Todos os produtos da categoria Esporte - Forward")
(pprint (db/todos-os-produtos-da-categoria-forward (d/db conn) "Esporte"))
(println "Todos os produtos da categoria Esporte - Backward")
(pprint (db/todos-os-produtos-da-categoria-backward (d/db conn) "Esporte"))
(println "Todos os produtos da categoria Eletronicos - Backward")
(pprint (db/todos-os-produtos-da-categoria-backward (d/db conn) "Eletrônicos"))

; Delete db
(db/apaga-banco)
