(ns curso1.aula1
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 1 - Schema e transações

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)

;Add computador entity
(let [computador (model/novo-produto "Computador Novo" "/computador_novo", 2500.10M)]
  (d/transact conn [computador]))

;Taking a snapshot from db
(def database1 (d/db conn))

(println "Finds only computador entity")
(pprint (d/q '[:find ?entidade
               :where [?entidade :produto/nome]] database1))

;Add celular entity
(let [celular (model/novo-produto "Celular caro", "/celular", 8888.10M)]
  (d/transact conn [celular]))

;Taking a new snapshot from db
(def database2 (d/db conn))

(println "Finds computador and celular entities")
(pprint (d/q '[:find ?entidade
               :where [?entidade :produto/nome]] database2))

(println "Shows names from both entities")
(pprint (d/q '[:find ?nome
               :where [_ :produto/nome ?nome]] database2))

; Delete db
(db/apaga-banco)
