(ns dev.demo.matrix.app)

(comment
  (do (require '[shadow.cljs.devtools.server :as shadow.server]
               '[shadow.cljs.devtools.api :as shadow])
      (shadow.server/start!)
      (shadow/watch :alex)
      (shadow/watch :bob))

  :repl/quit
  (shadow/repl :alex)

  :repl/quit
  (shadow/repl :bob))

