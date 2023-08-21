(ns indexes.db.config
  (:require [datomic.api :as d]))

(def db-url "datomic:dev://localhost:4334/ecommerce")

(defn abre-conexao []
  (d/create-database db-url)
  (d/connect db-url))

(defn apaga-banco []
  (d/delete-database db-url))

(def schema [{:db/ident       :person/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :person/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome da pessoa"}
             {:db/ident       :person/cpf
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/index       true
              :db/doc         "CPF da pessoa"}

             {:db/ident       :home/address
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Endereço da moradia"}
             {:db/ident       :home/person
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "Morador"}
             ])

(defn cria-schema [conn]
  (d/transact conn schema))
