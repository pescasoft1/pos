var RePrint = (function () {

    var lastSale = null;

    // 🔥 BUSCAR TICKET
    function buscar() {

        var id =
            document.getElementById('ticket-id').value;

        if (!id) {
            alert('Ingrese número de ticket');
            return;
        }

        fetch('/api/reimpresion/' + id)
            .then(r => r.json())
            .then(data => {

                if (!data.ok) {
                    alert('Ticket no encontrado');
                    return;
                }

                lastSale = data;

                render(data);

            });
    }

    // 🔥 RENDER TICKET
   function render(data) {
    reprintReceipt(data);
}

    // 🔥 WHATSAPP
    function whatsapp() {

        if (!lastSale) {
            alert('Primero busque un ticket');
            return;
        }

        var phone =
            document.getElementById(
                'ticket-phone'
            ).value;

        if (!phone) {
            alert('Ingrese WhatsApp');
            return;
        }

        var text =
            'Hola, gracias por su compra. ' +
            'Ticket #' +
            lastSale.sale.id +
            ' Total: $' +
            lastSale.sale.total;

        var url =
            'https://wa.me/52' +
            phone +
            '?text=' +
            encodeURIComponent(text);

        window.open(url, '_blank');
    }

    // 🔥 PDF
    async function pdf() {

        var ticket =
            document.getElementById(
                'ticket-print'
            );

        if (!ticket) {
            alert('No hay ticket');
            return;
        }

        const { jsPDF } = window.jspdf;

        html2canvas(ticket).then(canvas => {

            var img =
                canvas.toDataURL(
                    'image/png'
                );

            var pdf =
                new jsPDF({
                    orientation: 'portrait',
                    unit: 'mm',
                    format: [80, 150]
                });

            pdf.addImage(
                img,
                'PNG',
                5,
                5,
                70,
                0
            );

            pdf.save(
                'ticket-' +
                lastSale.sale.id +
                '.pdf'
            );

        });
    }
    function reprintReceipt(data) {

        var s = data.sale;

        s.items = data.items;

        var iva = s.total * 0.08;
        var totalFinal = s.total + iva;

        var rows = s.items.map(function (it) {

            return '<tr>'
                + '<td>' + it.nombre + '</td>'
                + '<td style="text-align:right">' + it.cantidad + '</td>'
                + '<td style="text-align:right">$'
                + parseFloat(it.subtotal).toFixed(2)
                + '</td>'
                + '</tr>';

        }).join('');

        var html = `
    <div id="ticket-print">

        <style>

            #ticket-print {
                font-family: monospace;
                font-size: 12px;
                width: 300px;
                margin: 20px auto;
            }

            table {
                width: 100%;
            }

            .line {
                border-top: 1px dashed #000;
                margin: 8px 0;
            }

        </style>

        <h2 style="text-align:center">
            ${s.site_name || 'POS'}
        </h2>

        <h3 style="text-align:center">
            Recibo de Venta
        </h3>

        <p style="text-align:center">
            Venta #${s.id}<br>
            ${s.fecha || ''}
        </p>

        <div class="line"></div>

        <table>

            <tr>
                <th>Producto</th>
                <th>Cant</th>
                <th>Total</th>
            </tr>

            ${rows}

        </table>

        <div class="line"></div>

        <table>

            <tr>
                <td>Subtotal:</td>
                <td style="text-align:right">
                    $${parseFloat(s.total).toFixed(2)}
                </td>
            </tr>

            <tr>
                <td>IVA (8%):</td>
                <td style="text-align:right">
                    $${iva.toFixed(2)}
                </td>
            </tr>

            <tr>
                <td><strong>Total:</strong></td>
                <td style="text-align:right">
                    <strong>
                        $${totalFinal.toFixed(2)}
                    </strong>
                </td>
            </tr>

        </table>

        <div class="line"></div>

        <p style="text-align:center">
            ¡Gracias por su compra!
        </p>

    </div>
    `;

        document.getElementById(
            'ticket-result'
        ).innerHTML = html;
    }
    return {
        buscar: buscar,
        whatsapp: whatsapp,
        pdf: pdf
    };

})();