(ns curso5.aula2
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso5.db.config :as db.config]
            [curso5.db.produto :as db.produto]
            [curso5.sample :as sample]
            [schema.core :as s]
            [curso5.db.venda :as db.venda]))

; Curso 5 - Datomic: banco filtrado e histórico
; Aula 2 - O problema do tempo

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
(println "Vendas")
(pprint (db.venda/get-all (d/db conn)))

(println "Custo da venda1 e da venda2")
(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

@(db.produto/add conn [{:produto/id    (:produto/id primeiro)
                        :produto/preco 30M}])

(println "Custo da venda1 e da venda2 com preço atualizado")
(pprint (db.venda/custo (d/db conn) venda1))
(pprint (db.venda/custo (d/db conn) venda2))

; Delete db
(db.config/apaga-banco)
