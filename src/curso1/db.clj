(ns curso1.db
  (:require [datomic.api :as d]))

; Follow readme to run datomic transactor

(def db-url "datomic:dev://localhost:4334/hello")

(defn abre-conexao []
  (d/create-database db-url)
  (d/connect db-url))

(defn apaga-banco []
  (d/delete-database db-url))

; Produtos
; id?
; nome String 1 ==> Computador Novo
; slug String 1 ==> /computador_novo
; preco ponto flutuante 1 ==> 3500.10

; id_entidade atributo        valor              transacao operacao
; 15          :produto/nome   Computador Novo    ID_TX     operacao
; 15          :produto/slug   /computador_novo   ID_TX     operacao
; 15          :produto/preco  3500.10M           ID_TX     operacao
; 17          :produto/nome   Telefone Caro      ID_TX     operacao
; 17          :produto/slug   /telefone          ID_TX     operacao
; 17          :produto/preco  8888.88M           ID_TX     operacao

; operacao true: colocou no banco
; operacao false: tirou do banco
(def schema [{:db/ident       :produto/nome
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O nome de um produto"}
             {:db/ident       :produto/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "O caminho para acessar esse produto via http"}
             {:db/ident       :produto/preco
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "O preço de um produto com precisão monetária"}
             {:db/ident       :produto/palavra-chave
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}])

(defn cria-schema [conn]
  (d/transact conn schema))

(defn todos-os-produtos [db]
  (d/q '[:find ?entidade
         :where [?entidade :produto/nome]] db))

(defn todos-os-produtos-por-slug [db slug]
  (d/q '[:find ?entidade
         :in $ ?slug
         :where [?entidade :produto/slug ?slug]]
       db slug))

(defn todos-os-slugs [db]
  (d/q '[:find ?slug
         :where [_ :produto/slug ?slug]]
       db))

(defn todos-os-produtos-com-seus-precos [db]
  (d/q '[:find ?nome ?preco
         :where [?e :produto/nome ?nome]
         [?e :produto/preco ?preco]]
       db))

(defn todos-os-produtos-com-pull-tudo [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :produto/nome]] db))

(defn todos-os-produtos-com-pull-especificado [db]
  (d/q '[:find (pull ?entidade [:produto/nome :produto/slug :produto/preco])
         :where [?entidade :produto/nome]] db))

(defn todos-os-produtos-com-usando-keys [db]
  (d/q '[:find ?nome ?preco
         :keys produto/nome produto/preco
         :where [?e :produto/nome ?nome]
         [?e :produto/preco ?preco]]
       db))

(defn todos-os-produtos-com-precos-acima-de [db preco-minimo]
  (d/q '[:find ?nome ?preco
         :in $ ?preco-minimo
         :where [?e :produto/preco ?preco]
         [(> ?preco ?preco-minimo)]
         [?e :produto/nome ?nome]]
       db preco-minimo))

(defn todos-os-produtos-por-palavra-chave [db palavra-chave]
  (d/q '[:find ?nome
         :in $ ?palavra-chave
         :where [?e :produto/nome ?nome]
         [?e :produto/palavra-chave ?palavra-chave]]
       db palavra-chave))
