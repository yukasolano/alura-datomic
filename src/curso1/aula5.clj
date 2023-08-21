(ns curso1.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 5 - Bancos filtrados e histórico

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

(let [computador (model/novo-produto "Computador Novo", "/computador-novo", 2500.10M)
      celular (model/novo-produto "Celular Caro", "/celular", 888888.10M)
      resultado @(d/transact conn [computador, celular])]
  (pprint resultado))

; meu snapshot, posso usar o momento real
(def fotografia-no-passado (d/db conn))

(let [calculadora {:produto/nome "Calculadora com 4 operações"}
      celular-barato (model/novo-produto "Celular Barato", "/celular-barato", 0.1M)
      resultado @(d/transact conn [calculadora, celular-barato])]
  (pprint resultado))

(println "All products now")
(pprint (count (db/todos-os-produtos (d/db conn))))

(println "All products at some moment in the past")
(pprint (count (db/todos-os-produtos fotografia-no-passado)))

;Também é possivel especificar um momento especifico
;(pprint (count (db/todos-os-produtos (d/as-of (d/db conn) #inst "2021-07-16T20:31:55.700"))))

; Delete db
(db/apaga-banco)
