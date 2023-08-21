(ns curso1.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 4 - Pull

; Follow readme to run datomic transactor

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

(println "Usando pull com *")
(pprint (db/todos-os-produtos-com-pull-tudo (d/db conn)))

(println "Usando pull especificando campos")
(pprint (db/todos-os-produtos-com-pull-especificado (d/db conn)))

(println "Retorna mapa usando :keys")
(pprint (db/todos-os-produtos-com-usando-keys (d/db conn)))


; Delete db
(db/apaga-banco)
