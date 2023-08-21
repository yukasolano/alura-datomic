(ns curso4.model
  (:require [schema.core :as s])
  (:import (java.util UUID)))


(s/def Categoria
  {:categoria/id   UUID
   :categoria/nome s/Str})

(s/def Variacao
  {:variacao/id    UUID
   :variacao/nome  s/Str
   :variacao/preco BigDecimal})

(s/def Produto
  {:produto/id                             UUID
   (s/optional-key :produto/nome)          s/Str
   (s/optional-key :produto/slug)          s/Str
   (s/optional-key :produto/preco)         BigDecimal
   (s/optional-key :produto/palavra-chave) [s/Str]
   (s/optional-key :produto/categoria)     Categoria
   (s/optional-key :produto/estoque)       s/Int
   (s/optional-key :produto/digital)       s/Bool
   (s/optional-key :produto/variacao)      [Variacao]
   (s/optional-key :produto/visualizacoes) s/Int})

(defn uuid [] (UUID/randomUUID))

(s/defn novo-produto :- Produto
  ([nome slug preco] (novo-produto (uuid) nome slug preco))
  ([uuid nome slug preco] (novo-produto uuid nome slug preco 0))
  ([uuid nome slug preco estoque]
   {:produto/id      uuid
    :produto/nome    nome
    :produto/slug    slug
    :produto/preco   preco
    :produto/estoque estoque
    :produto/digital false}))

(s/defn nova-categoria :- Categoria
  ([nome] (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid
    :categoria/nome nome}))
