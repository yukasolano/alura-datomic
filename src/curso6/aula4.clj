(ns curso6.aula4
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [curso6.db.config :as db.config]
            [curso6.db.produto :as db.produto]
            [curso6.sample :as sample]
            [schema.core :as s]
            [schema-generators.generators :as g]
            [curso6.model :as model]
            [curso6.generators :as generators])
  (:import (java.util UUID)
           (schema.core OptionalKey)))

; Curso 6 - Datomic: geradores, schemas e índices
; Aula 4 - Geradores de schemas

; Follow readme to run datomic transactor

;Open db conection with transactor datomic:dev://localhost:4334/ecommerce
(db.config/apaga-banco)
(def conn (db.config/abre-conexao))

;Create schemas
(db.config/cria-schema conn)

(s/set-fn-validation! true)

;Add sample data
(sample/adiciona-dados conn)

;Get all products
(def produtos (db.produto/get-all (d/db conn)))

(defn extrai-nome-da-chave [chave]
  (cond
    (keyword? chave) chave
    (instance? OptionalKey chave) (get chave :k)
    :else chave))

(defn propriedades-do-valor [valor]
  (if (vector? valor)
    (->> valor
         first
         propriedades-do-valor
         (merge {:db/cardinality :db.cardinality/many}))
    (cond (= valor UUID) {:db/valueType :db.type/uuid
                          :db/unique    :db.unique/identity}
          (= valor s/Str) {:db/valueType :db.type/string}
          (= valor BigDecimal) {:db/valueType :db.type/bigdec}
          (= valor Long) {:db/valueType :db.type/long}
          (= valor Boolean) {:db/valueType :db.type/boolean}
          (map? valor) {:db/valueType :db.type/ref}
          :else {:db/valueType (str "desconhecido: " valor)})))

(defn chave-valor-para-definicao [[chave valor]]
  (let [base  {:db/ident       (extrai-nome-da-chave chave)
               :db/cardinality :db.cardinality/one}
        extra (propriedades-do-valor valor)]
    (merge base extra)))

(defn schema-to-datomic [definicao]
  (mapv chave-valor-para-definicao definicao))

(println "Datomic for Categoria")
(pprint (schema-to-datomic model/Categoria))

(println "Datomic for Variacao")
(pprint (schema-to-datomic model/Variacao))

(println "Datomic for Venda")
(pprint (schema-to-datomic model/Venda))


(println "Datomic for Produto")
(pprint (schema-to-datomic model/Produto))

; Delete db
;(db.config/apaga-banco)
