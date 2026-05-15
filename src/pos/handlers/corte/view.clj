(ns pos.handlers.corte.view)

(defn main-view [desde hasta]
  [:div.container-fluid.mt-3
   [:div.card.shadow-sm
    [:div.card-body
     [:h3.mb-3 "Corte de ventas diarias"]

     [:div.row.g-2.align-items-end
      [:div.col-md-3
       [:label.form-label "Desde"]
       [:input#desde.form-control
        {:type "date"
         :value desde}]]
      [:div.col-md-3
       [:label.form-label "Hasta"]
       [:input#hasta.form-control
        {:type "date"
         :value hasta}]]
      [:div.col-md-6.d-flex.align-items-end.gap-2
       [:button.btn.btn-primary
        {:onclick "Corte.buscar()"}
        "Buscar"]

       [:button.btn.btn-success
        {:onclick "Corte.imprimir()"}
        "Imprimir"]

       [:button.btn.btn-danger
        {:onclick "Corte.pdf()"}
        "PDF"]]]

     [:hr.my-3]

     [:div#corte-result]]]

   [:style
    "
    #corte-result{
      width: 100%;
      display: block;
    }

    .corte-receipt{
      width: 72mm;
      max-width: 72mm;
      margin: 0 auto;
      background: #fff;
      padding: 3mm;
      font-family: monospace;
      font-size: 10px;
      color: #000;
      border: none;
      box-shadow: none;
    }

    .corte-header{
      text-align: center;
      margin-bottom: 4mm;
    }

    .corte-header h2{
      font-size: 16px;
      margin: 0;
      font-weight: bold;
      color: #2196f3;
      line-height: 1.1;
    }

    .corte-header h3{
      font-size: 11px;
      margin: 1mm 0 1mm 0;
      font-weight: normal;
      color: #2196f3;
      line-height: 1.1;
    }

    .corte-header .meta{
      font-size: 9px;
      margin-top: 1mm;
    }

    .corte-summary{
      margin: 2mm 0 3mm 0;
      padding-bottom: 2mm;
      border-bottom: 1px dashed #999;
    }

    .corte-summary .row-line,
    .corte-sale .row-line{
      display: flex;
      justify-content: space-between;
      gap: 2mm;
      line-height: 1.2;
      white-space: nowrap;
    }

    .corte-sale{
      padding: 2mm 0 2mm 0;
      border-bottom: 1px dashed #999;
    }

    .corte-sale:last-child{
      border-bottom: none;
      padding-bottom: 0;
    }

    .corte-sale .sale-title{
      display: flex;
      justify-content: space-between;
      gap: 2mm;
      font-weight: bold;
      margin-bottom: 1mm;
      font-size: 10px;
    }

    .corte-sale .sale-meta{
      display: flex;
      justify-content: space-between;
      gap: 2mm;
      margin-bottom: 1mm;
      font-size: 9px;
    }

    .corte-items{
      width: 100%;
      border-collapse: collapse;
      margin: 1mm 0 2mm 0;
      table-layout: fixed;
    }

    .corte-items th,
    .corte-items td{
      padding: 0.5mm 0;
      vertical-align: top;
      word-break: break-word;
    }

    .corte-items th{
      text-align: left;
      font-weight: bold;
      border-bottom: 1px dotted #bbb;
      padding-bottom: 1mm;
      font-size: 9px;
    }

    .corte-items th:nth-child(2),
    .corte-items td:nth-child(2){
      width: 12mm;
      text-align: center;
    }

    .corte-items th:nth-child(3),
    .corte-items td:nth-child(3){
      width: 18mm;
      text-align: right;
    }

    .corte-total{
      margin-top: 1mm;
      display: flex;
      justify-content: space-between;
      gap: 2mm;
      font-weight: bold;
      font-size: 10px;
    }

    .corte-empty{
      padding: 3mm;
      background: #fff;
      border: none;
    }

    @media print{
      @page{
        size: 72mm auto;
        margin: 0;
      }

      body{
        margin: 0;
        padding: 0;
      }

      body *{
        visibility: hidden;
      }

      #corte-result,
      #corte-result *{
        visibility: visible;
      }

      #corte-result{
        position: absolute;
        left: 0;
        top: 0;
        width: 72mm;
      }

      .corte-receipt{
        width: 72mm;
        max-width: 72mm;
        margin: 0;
        padding: 3mm;
        border: none;
        box-shadow: none;
      }

      .corte-sale{
        page-break-inside: avoid;
      }
    }
    "]

   [:script
    {:src "https://html2canvas.hertzen.com/dist/html2canvas.min.js"}]

   [:script
    {:src "https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"}]

   [:script
    {:src "/js/corte.js?v=4"}]])