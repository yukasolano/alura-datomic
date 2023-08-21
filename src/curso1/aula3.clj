(ns curso1.aula3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 3 - Mais queries

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Add entities
(let [computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)]
  (d/transact conn [computador, celular, calculadora, celular-barato]))

(println "All products")
(pprint (db/todos-os-produtos (d/db conn)))

(println "All products by slug: /computador-novo")
(pprint (db/todos-os-produtos-por-slug (d/db conn) "/computador-novo"))

(println "All slugs")
(pprint (db/todos-os-slugs (d/db conn)))

(println "All products with their prices")
(pprint (db/todos-os-produtos-com-seus-precos (d/db conn)))

; Delete db
(db/apaga-banco)
