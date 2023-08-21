# Formação de Datomic na Alura


---

# Configurar datomic

1. Baixar Datomic Stater do site:
https://www.datomic.com/get-datomic.html

2. Unzip e escolha uma pasta para adicionar os arquivos baixados

3. Pegue o template `dev-transactor-template.properties` da pasta config/samples e coloque sua licença
```
license-key=<ADD-YOUR-LICENSE-KEY>
```

4. Copie esse config para a pasta config 

5. Abrir terminal e rodar os comandos:
```
cd /<caminho-da-pasta-do-datomic>/datomic-pro-1.0.6269
bin/maven-install
```
6. No IntelliJ, adicionar a seguinte dependência no arquivo project.clj:
```
[com.datomic/datomic-pro "1.0.6269"]
```
7. No IntelliJ, esperar ou forçar a atualização dos projetos Leiningen



# Rodar datomic
1. No terminal, rodar os comandos:
```
cd /<caminho-da-pasta-do-datomic>/datomic-pro-1.0.6269
bin/transactor config/dev-transactor-template.properties
```


# Abrir o explorador do datomic
```bin/console -p 8080 dev datomic:dev://localhost:4334/```

E depois abrir no browser localhost:8080/browse
# alura-datomic
