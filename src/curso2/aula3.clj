(ns curso2.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso2.db :as db]
            [curso2.model :as model]))

; Curso 2 - Datomic Queries: avançando com o modelo e pesquisas
; Aula 3 - Referenciando entidades

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Create categorias
(def eletronicos (model/nova-categoria "Eletrônicos"))
(def esporte (model/nova-categoria "Esporte"))
(pprint @(db/adiciona-categorias conn [eletronicos, esporte]))

(println "All categories")
(def categorias (db/todas-as-categorias (d/db conn)))
(pprint categorias)

;Create products
(def computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M))
(def celular (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M))
(def calculadora {:produto/nome "Calculadora com 4 operações"})
(def celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M))
(def xadrez (model/novo-produto "Tabuleiro de xadrez", "tabuleiro-de-xadrez", 30M))
(pprint @(db/adiciona-produtos conn [computador, celular, calculadora, celular-barato, xadrez]))

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

;Add eletronicos category to products
(db/atribui-categorias conn [computador, celular, celular-barato] eletronicos)

(println "Show computador with category")
(pprint (db/um-produto (d/db conn) (:produto/id computador)))

;Add esporte category to product
(db/atribui-categorias conn [xadrez] esporte)

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

; Delete db
(db/apaga-banco)
