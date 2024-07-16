(defproject autorizador "0.1.0-SNAPSHOT"
  :description "Um autorizador simples de transações de crédito"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [ring/ring-core "1.12.2"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.1"]
                 [http-kit "2.8.0"]
                 [prismatic/schema "1.4.1"]]
  :main ^:skip-aot autorizador.server
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
