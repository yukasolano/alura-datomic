(ns curso2.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso2.db :as db]
            [curso2.model :as model]))

; Curso 2 - Datomic Queries: avançando com o modelo e pesquisas
; Aula 1 - Identificadores únicos e identidade

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Add entities
(let [computador (model/novo-produto (model/uuid) "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto (model/uuid) "Celular Caro", "/celular", 888888.10M)
      calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)]
  (pprint @(d/transact conn [computador, celular, calculadora, celular-barato])))

(println "All products")
(def produtos (db/todos-os-produtos (d/db conn)))
(pprint produtos)

(def primeiro-dbid (-> produtos ffirst :db/id))
(println "O dbid do primeiro produto é" primeiro-dbid)
(pprint (db/um-produto-por-dbid (d/db conn) primeiro-dbid))

(def primeiro-produto-uuid (-> produtos ffirst :produto/id))
(println "O id do primeiro produto é" primeiro-produto-uuid)
(pprint (db/um-produto (d/db conn) primeiro-produto-uuid))

; Delete db
(db/apaga-banco)
