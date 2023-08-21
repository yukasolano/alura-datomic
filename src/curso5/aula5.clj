(ns curso5.aula5
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso5.db.config :as db.config]
            [curso5.db.produto :as db.produto]
            [curso5.sample :as sample]
            [schema.core :as s]
            [curso5.db.venda :as db.venda]))

; Curso 5 - Datomic: banco filtrado e histórico
; Aula 5 - Otimizações com histórico e joins de filtros

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/ecommerce
(def conn (db.config/abre-conexao))

;Create schemas
(db.config/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db.produto/get-all (d/db conn)))

;Get first product
(println "First product")
(def primeiro (first produtos))
(pprint primeiro)

(def venda1 (db.venda/add conn (:produto/id primeiro) 3))
(def venda2 (db.venda/add conn (:produto/id primeiro) 4))
(def venda3 (db.venda/add conn (:produto/id primeiro) 8))

@(db.venda/update-status conn venda1 "preparando")
@(db.venda/update-status conn venda2 "preparando")
@(db.venda/update-status conn venda2 "em transito")
@(db.venda/update-status conn venda2 "entregue")

(println "Historico venda1")
(pprint (db.venda/historico (d/db conn) venda1))

(println "Historico venda2")
(pprint (db.venda/historico (d/db conn) venda2))

(println "Cancelando venda1 ...")
@(db.venda/update-status conn venda1 "cancelada")

(println "Historico venda1")
(pprint (db.venda/historico (d/db conn) venda1))

(println "Quantidade de vendas ativas:"
         (count (db.venda/get-all-ids-status-not-canceled (d/db conn))))

(println "Quantidade de vendas cancelas:"
         (count (db.venda/get-all-ids-status-canceled (d/db conn))))

(println "Quantidade de vendas totais (ativas e canceladas):"
         (count (db.venda/get-all-ids-status-inclusive-canceled (d/db conn))))

(println "--------------------------")
(println "Historico geral desde:")
(pprint (db.venda/historico-geral (d/db conn) #inst "2022-08-06T00:36:22.435-00:00"))

; Delete db
(db.config/apaga-banco)
