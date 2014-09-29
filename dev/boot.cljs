(ns dev.boot
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs-time.local :as ltime]
            [xn.ui.calendar :as cal]
            [xn.library.popup :as p]
            [figwheel.client :as fw]))

(enable-console-print!)

(p/init-popups (js/document.getElementById "popups"))

(defn demo [cur owner]
  (om/component
    (dom/div
      nil
      (dom/h1 nil "Dropdown controls in various arrangements")
      (om/build cal/calendar-component cur {:opts {:key :date}})
      (om/build cal/datetime-component cur {:opts {:key :date}})
      (om/build cal/date-range-component cur {:opts {:start :date :end :end-date :time? true}})
      (dom/h2 nil "Static versions with no events registered (easy to compose into your own controls, etc)")
      (cal/calendar {:active-date (:date cur)})
      (cal/clock (:date cur) true 0.4 nil)
      (cal/clock (:date cur) false 0.4 nil))))

(om/root demo
         {:date (ltime/local-now)
          :end-date (ltime/local-now)}
         {:target (js/document.getElementById "contents")})
