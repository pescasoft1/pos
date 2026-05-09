function buildTicketHTML(s) {

    var iva = s.total * 0.08;
    var totalFinal = s.total + iva;

    var rows = s.items.map(function (it) {

        return `
        <tr>
            <td>${it.nombre}</td>
            <td style="text-align:center">
                ${it.cantidad}
            </td>
            <td style="text-align:right">
                $${(it.cantidad * it.precio).toFixed(2)}
            </td>
        </tr>
        `;

    }).join('');

    return `
    <div id="ticket-print">

        <style>

            #ticket-print {
                font-family: monospace;
                width: 300px;
                margin:auto;
                font-size:12px;
            }

            #ticket-print table {
                width:100%;
                border-collapse:collapse;
            }

            #ticket-print td,
            #ticket-print th {
                padding:4px;
            }

            .line {
                border-top:1px dashed #000;
                margin:6px 0;
            }

        </style>

        <div style="text-align:center">

            <h2>${s.site_name || 'POS'}</h2>

            <h3>Ticket de Venta</h3>

            <div>
                Ticket #${s.id || s.venta_id}
            </div>

            <div>
                ${s.fecha || ''}
            </div>

        </div>

        <div class="line"></div>

        <table>

            <thead>

                <tr>
                    <th>Producto</th>
                    <th>Cant</th>
                    <th>Total</th>
                </tr>

            </thead>

            <tbody>

                ${rows}

            </tbody>

        </table>

        <div class="line"></div>

        <table>

            <tr>
                <td>Subtotal:</td>
                <td style="text-align:right">
                    $${s.total.toFixed(2)}
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

        <div style="text-align:center">
            ¡Gracias por su compra!
        </div>

    </div>
    `;
}