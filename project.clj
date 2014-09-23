(defproject xn.ui.calendar "1.0.0"
  :description "Calendar picker with date-range and time abilities"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [com.andrewmcveigh/cljs-time "0.1.6"]
                 [om "0.7.3"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.4-SNAPSHOT"]]

  :source-paths ["src"]

  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src" "dev"]
             :compiler {:output-to "resources/public/js/compiled/xn/ui/calendar.js"
                        :output-dir "resources/public/js/compiled/out"
                        :optimizations :none
                        :source-map true}}
            {:id "min"
             :source-paths ["src"]
             :compiler {:output-to "www/xn/ui/calendar.min.js"
                        :optimizations :advanced
                        :pretty-print false
                        :preamble ["react/react.min.js"]
                        :externs ["react/externs/react.js"]}}]}
  :figwheel
  {:http-server-root "public" ;; default and assumes "resources"
   :server-port 3450 ;; default + 1
   :css-dirs ["public/resources/css"]})
