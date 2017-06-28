(ns beautiful-code.core
  (:require
    [clojure.core.match :as match]
    [proto-repl.saved-values :as proto]))


;; TODO: replace core.match with clojure spec
(defn eval-expr
  [expr env]
  (match/match [expr]
    [(['fn [s] body] :seq)]   (fn [v]
                                (eval-expr
                                  body
                                  (fn [k]
                                    (if (= s k)
                                      v
                                      (env k)))))
    [([rator rand] :seq)]     ((eval-expr rator env)
                               (eval-expr rand env))
    ;; Put "symbol" match last rather than first b/c it's easier to use a
    ;; "catch all" for lookups.
    [a]                       (env a)))


;; and some examples
(comment
  ;; a symbol that's in the environment
  ;; => 1
  (eval-expr 'a (fn [x]
                  (if (= x 'a)
                    1
                    (throw (Exception. "Symbol not found!")))))


  ;; a symbol that's not in the environment
  ;; => Exception "Symbol not found!"
  (eval-expr 'b (fn [x]
                  (if (= x 'a)
                    1
                    (throw (Exception. "Symbol not found!")))))


  ;; function definition and application
  ;; => 2
  (eval-expr
    '((fn [x] x) 2)
    (fn [y]
      (cond
        (number? y) y
        ;; default
        :else       (throw (Exception. "Symbol not found!")))))


  ;; function definition and application with a "built-in" function "inc" that
  ;; needs to come from the environment
  ;; => 3
  (eval-expr
    '((fn [x] (inc x)) 2)
    (fn [y]
      (cond
        (number? y) y
        (= y 'inc)  clojure.core/inc
        ;; default
        :else       (throw (Exception. "Symbol not found!"))))))
