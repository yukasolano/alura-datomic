(ns curso1.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso1.db :as db]
            [curso1.model :as model]))

; Curso 1 - Datomic: um banco cronológico
; Aula 2 - Retract, updates e organização

;Open db conection with transactor datomic:dev://localhost:4334/hello
(def conn (db/abre-conexao))

;Create schemas
(db/cria-schema conn)


; o datomic suporta somente um dos identificadores, claro, não foi imposta nenhuma restrição
(let [calculadora {:produto/nome "Calculadora com 4 operações"}]
  (d/transact conn [calculadora]))

; não funciona pois se você quer algo "vazio", é só não colocar
; (let [radio-relogio {:produto/nome "Rádio com relógio" :produto/slug nil}]
;  (d/transact conn [radio-relogio]))

;Update and remove operations
(let [celular-barato (model/novo-produto "Celular barato", "/celular-barato", 8888.10M)
      resultado @(d/transact conn [celular-barato])
      id-identidade (-> resultado :tempids vals first)]

  (println "Result from adding new entity")
  (pprint resultado)

  (println "Update preco")
  (pprint @(d/transact conn [[:db/add id-identidade :produto/preco 0.1M]])) ; update preco

  (println "Remove slug")
  (pprint @(d/transact conn [[:db/retract id-identidade :produto/slug "/celular-barato"]]))) ; delete slug

; Delete db
(db/apaga-banco)
