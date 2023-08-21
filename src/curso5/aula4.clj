(ns curso5.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso5.db.config :as db.config]
            [curso5.db.produto :as db.produto]
            [curso5.sample :as sample]
            [schema.core :as s]
            [curso5.db.venda :as db.venda]))

; Curso 5 - Datomic: banco filtrado e histórico
; Aula 4 - History

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

(println "Quantidade de vendas ativas:"
         (count (db.venda/get-all-ids (d/db conn))))


(println "Cancelando uma das vendas ...")
@(db.venda/cancel conn venda1)

(println "Quantidade de vendas ativas:"
         (count (db.venda/get-all-ids (d/db conn))))

(println "Quantidade de vendas cancelas:"
         (count (db.venda/get-all-ids-canceled (d/db conn))))

(println "Quantidade de vendas totais (ativas e canceladas):"
         (count (db.venda/get-all-ids-inclusive-canceled (d/db conn))))

(println "-----------------------------")
(println "Atualizando preço para 30 ...")
@(db.produto/add conn [{:produto/id    (:produto/id primeiro)
                        :produto/preco 30M}])
(println "Atualizando preço para 40 ...")
@(db.produto/add conn [{:produto/id    (:produto/id primeiro)
                        :produto/preco 40M}])
(println "Atualizando preço para 50 ...")
@(db.produto/add conn [{:produto/id    (:produto/id primeiro)
                        :produto/preco 50M}])
(println "Atualizando preço para 60 ...")
@(db.produto/add conn [{:produto/id    (:produto/id primeiro)
                        :produto/preco 60M}])

(println "Histórico de preços")
(pprint (db.produto/historico-de-precos (d/db conn) (:produto/id primeiro)))

; Delete db
(db.config/apaga-banco)
