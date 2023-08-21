(ns curso2.model
  (:import (java.util UUID)))

(defn uuid [] (UUID/randomUUID))

(defn novo-produto
  ([nome slug preco] (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco]
   {:produto/id    uuid
    :produto/nome  nome
    :produto/slug  slug
    :produto/preco preco}))

(defn nova-categoria
  ([nome] (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id uuid
    :categoria/nome nome}))
