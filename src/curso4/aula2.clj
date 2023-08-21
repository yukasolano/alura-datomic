(ns curso4.aula2
(:use clojure.pprint)
(:require [datomic.api :as d]
  [curso4.db :as db]
  [curso4.sample :as sample]
  [schema.core :as s]))

; Curso 4 - Datomic: Bindings, transaction functions e filters
; Aula 2 - Transaction functions

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db/todos-os-produtos (d/db conn)))

(println "First product: updating only one attribute")
(def primeiro (first produtos))
(pprint primeiro)

(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 2500.10M 30M))
(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 30M 35M))
;Throws exceptions because previous value is not the expected
;(pprint @(db/atualiza-preco! conn (:produto/id primeiro) 30M 45M))

(println "Second product: updating multiple attributes")
(def segundo (second produtos))
(pprint segundo)

(def a-atualizar {:produto/id (:produto/id segundo) :produto/preco 3000M :produto/slug "/test"})
(pprint @(db/atualiza-produto! conn segundo a-atualizar))

; Delete db
(db/apaga-banco)
