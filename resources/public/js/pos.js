var POS = (function () {
    'use strict';

    var cart = [];
    var lastSale = null;
    var allProducts = [];
    var qrCodeCache = {};
    var tipoCambio = 1;

    function generateQRBase64(text, size) {
        var key = text + '_' + size;
        if (qrCodeCache[key]) return qrCodeCache[key];

        if (typeof QRCode === 'undefined') {
            return null;
        }
        var canvas = document.createElement('canvas');
        try {
            QRCode.toCanvas(canvas, text, { width: size, margin: 1 });
            qrCodeCache[key] = canvas.toDataURL();
            return qrCodeCache[key];
        } catch (e) {
            return null;
        }
    }

    function makeProductQRText(productId) {
        return 'PROD-' + productId;
    }

    function getProductQR(productId) {
        var qrText = makeProductQRText(productId);
        var qrBase64 = generateQRBase64(qrText, 60);
        if (!qrBase64) {
            return '<span class="text-muted" style="font-size:10px;">QR</span>';
        }
        return '<img src="' + qrBase64 + '" style="width:40px;height:40px;border:1px solid #ddd;border-radius:3px;" title="QR: ' + qrText + '">';
    }

    function getProductData() {
        var el = document.getElementById('pos-app');
        if (!el) return [];
        try { return JSON.parse(el.getAttribute('data-productos') || '[]'); }
        catch (e) { return []; }
    }

    function getExchangeRate() {
        var el = document.getElementById('pos-app');
        var value = el ? parseFloat(el.getAttribute('data-tipo-cambio')) : 1;
        return value > 0 ? value : 1;
    }

    function init() {
        allProducts = getProductData();
        tipoCambio = getExchangeRate();
        loadQuoteCart();
        var input = document.getElementById('pos-search');
        if (input) {
            // Live search: show/hide cards as the user types (non-numeric)
            input.addEventListener('input', function () {
                var term = this.value.trim();
                // Only do client-side filtering for non-numeric search
                if (!/^\d+$/.test(term)) {
                    var lowerTerm = term.toLowerCase();
                    document.querySelectorAll('.pos-product-card').forEach(function (card) {
                        var name = (card.getAttribute('data-nombre') || '').toLowerCase();
                        card.classList.toggle('hidden', !(!term || name.indexOf(term) !== -1));
                    });
                }
            });
            // Barcode scanner support: detect Enter key on numeric input
            input.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    var term = this.value.trim();
                    if (/^\d+$/.test(term)) {
                        e.preventDefault();
                        handleBarcodeScan(term);
                    }
                }
            });
        }
    }

    var quoteId = null;

    function loadQuoteCart() {
        try {
            var stored = localStorage.getItem('cotizacion_cart');
            if (!stored) return;
            var data = JSON.parse(stored);
            if (!data || !Array.isArray(data.items) || data.items.length === 0) return;
            quoteId = data.cotizacion_id || null;
            cart = data.items.map(function (item) {
                return {
                    producto_id: item.producto_id,
                    nombre: item.nombre,
                    precio: parseFloat(item.precio) || 0,
                    cantidad: item.cantidad || 1,
                    categoria: item.categoria || ''
                };
            });
            localStorage.removeItem('cotizacion_cart');
            renderCart();
        } catch (e) {
            console.warn('No quote cart loaded', e);
        }
    }

    function handleBarcodeScan(barcode) {
        var csrfInput = document.querySelector('input[name="__anti-forgery-token"]');
        var csrfToken = csrfInput ? csrfInput.value : '';
        var input = document.getElementById('pos-search');

        fetch('/api/pos/search?q=' + encodeURIComponent(barcode), {
            method: 'GET',
            headers: {
                'X-CSRF-Token': csrfToken,
                'x-requested-with': 'XMLHttpRequest'
            }
        })
            .then(function (resp) { return resp.json(); })
            .then(function (data) {
                if (data.ok && data.scanned) {
                    addItem(data.scanned.id);
                    input.value = '';
                    // Reset card visibility
                    document.querySelectorAll('.pos-product-card').forEach(function (card) {
                        card.classList.remove('hidden');
                    });
                } else if (data.ok && data.data && data.data.length === 0) {
                    alert('Producto no encontrado');
                }
            })
            .catch(function (err) {
                console.error('Barcode scan error:', err);
            });
    }

    // --- Cart helpers ---
    function findProduct(id) {
        for (var i = 0; i < allProducts.length; i++) {
            if (allProducts[i].id == id) return allProducts[i];
        }
        return null;
    }

    function findCartItem(id) {
        for (var i = 0; i < cart.length; i++) {
            if (cart[i].producto_id == id) return i;
        }
        return -1;
    }

    function addItem(productId) {
        var product = findProduct(productId);
        if (!product) return;
        var idx = findCartItem(productId);
        if (idx >= 0) {
            cart[idx].cantidad++;
        } else {
            cart.push({
                producto_id: product.id,
                nombre: product.nombre,
                precio: parseFloat(product.precio) || 0,
                cantidad: 1,
                stock: parseInt(product.stock) || 0,
                categoria: product.categoria || ''
            });
        }
        renderCart();
    }

    function addMiscCharge() {
        var name = prompt('Nombre del cargo misc. (ej: Manode obra):');
        if (!name || !name.trim()) return;
        var priceStr = prompt('Monto del cargo:');
        var price = parseFloat(priceStr);
        if (isNaN(price) || price <= 0) {
            alert('Monto inválido');
            return;
        }
        var miscId = 'MISC-' + Date.now();
        cart.push({
            producto_id: null,
            nombre: name.trim(),
            precio: price,
            cantidad: 1,
            stock: 0,
            categoria: 'misc'
        });
        renderCart();
    }

    function findCartItemByName(name) {
        for (var i = 0; i < cart.length; i++) {
            if (cart[i].nombre === name && cart[i].producto_id === null) return i;
        }
        return -1;
    }

    function removeItem(productId, isMisc) {
        var idx;
        if (isMisc) {
            idx = findCartItemByName(productId);
        } else {
            idx = findCartItem(productId);
        }
        if (idx >= 0) { cart.splice(idx, 1); renderCart(); }
    }

    function updateQty(productId, delta, isMisc) {
        var idx;
        if (isMisc) {
            idx = findCartItemByName(productId);
        } else {
            idx = findCartItem(productId);
        }
        if (idx < 0) return;
        cart[idx].cantidad += delta;
        if (cart[idx].cantidad <= 0) cart.splice(idx, 1);
        renderCart();
    }

    function getSubtotal() {
        return cart.reduce(function (sum, item) {
            return sum + item.cantidad * item.precio;
        }, 0);
    }

    function getDiscount() {
        var subtotal = getSubtotal();
        var typeEl = document.getElementById('pos-discount-type');
        var valueEl = document.getElementById('pos-discount-value');
        var type = typeEl ? typeEl.value : 'amount';
        var value = valueEl ? (parseFloat(valueEl.value) || 0) : 0;

        if (value <= 0 || subtotal <= 0) return 0;
        var discount = type === 'percent' ? subtotal * Math.min(value, 100) / 100 : value;
        return Math.min(discount, subtotal);
    }

    function getTotal() {
        return Math.max(getSubtotal() - getDiscount(), 0);
    }

    function getCurrency() {
        var monedaEl = document.getElementById('pos-moneda');
        return monedaEl ? monedaEl.value : 'MXN';
    }

    function getPaymentAmount() {
        return parseFloat(document.getElementById('pos-payment').value) || 0;
    }

    function getPaymentInMXN() {
        var payment = getPaymentAmount();
        return getCurrency() === 'USD' ? payment * tipoCambio : payment;
    }

    // --- Render the cart panel ---
    function renderCart() {
        var container = document.getElementById('pos-cart-items');
        var subtotalEl = document.getElementById('pos-subtotal');
        var discountEl = document.getElementById('pos-discount-amount');
        var totalEl = document.getElementById('pos-total');
        var registerBtn = document.getElementById('pos-register-btn');

        if (cart.length === 0) {
            container.innerHTML = '<p class="text-muted text-center">Carrito vacío</p>';
            subtotalEl.textContent = '$0.00';
            discountEl.textContent = '$0.00';
            totalEl.textContent = '$0.00';
            var totalUsdEl = document.getElementById('pos-total-usd');
            if (totalUsdEl) totalUsdEl.textContent = 'US$0.00';
            registerBtn.disabled = true;
            calcChange();
            return;
        }

        var html = '';
        cart.forEach(function (item) {
            var subtotal = (item.cantidad * item.precio).toFixed(2);
            var isMisc = item.producto_id === null;
            var escapedName = escapeHtml(item.nombre).replace(/'/g, "\\'");
            html += '<div class="pos-cart-row">';
            html += '<div class="pos-cart-qr">' + (isMisc ? '<span class="text-muted">-</span>' : getProductQR(item.producto_id)) + '</div>';
            html += '<span class="pos-cart-name">' + escapeHtml(item.nombre) + '</span>';
            html += '<span class="pos-cart-qty">';






            if (isMisc) {
                html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                    + " onclick=\"POS.updateQty('" + escapedName + "', -1, true)\">-</button>";
            } else {
                html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                    + ' onclick="POS.updateQty(' + item.producto_id + ', -1, false)">-</button>';
            }
            html += '<strong class="mx-1">' + item.cantidad + '</strong>';
            if (isMisc) {
                html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                    + " onclick=\"POS.updateQty('" + escapedName + "', 1, true)\">+</button>";
            } else {
                html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                    + ' onclick="POS.updateQty(' + item.producto_id + ', 1, false)">+</button>';
            }
            html += '</span>';
            if (isMisc) {
                html += '<span class="pos-cart-remove"'
                    + " onclick=\"POS.removeItem('" + escapedName + "', true)\">"
                    + '<i class="bi bi-x-circle"></i></span>';
            } else {
                html += '<span class="pos-cart-remove"'
                    + ' onclick="POS.removeItem(' + item.producto_id + ', false)">'
                    + '<i class="bi bi-x-circle"></i></span>';
            }
            html += '</div>';
        });

        container.innerHTML = html;
        subtotalEl.textContent = '$' + getSubtotal().toFixed(2);
        discountEl.textContent = '$' + getDiscount().toFixed(2);
        totalEl.textContent = '$' + getTotal().toFixed(2);
        var totalUsdEl = document.getElementById('pos-total-usd');
        if (totalUsdEl) totalUsdEl.textContent = 'US$' + (getTotal() / tipoCambio).toFixed(2);

        var total = getTotal();
        var iva = total * 0.08;
        var totalFinal = total + iva;

        var ivaEl = document.getElementById('pos-iva');
        if (ivaEl) ivaEl.textContent = "$" + iva.toFixed(2);

        var totalFinalEl = document.getElementById('pos-total-final');
        if (totalFinalEl) totalFinalEl.textContent = "$" + totalFinal.toFixed(2);
        registerBtn.disabled = false;
        calcChange();
    }

    function calcChange() {
        var subtotalEl = document.getElementById('pos-subtotal');
        var discountEl = document.getElementById('pos-discount-amount');
        var totalEl = document.getElementById('pos-total');
        var total = getTotal();
        var iva = total * 0.08;
        var totalFinal = total + iva;

        var moneda = getCurrency();
        var pago = getPaymentInMXN();
        var cambio = pago - total;

        if (subtotalEl) subtotalEl.textContent = '$' + getSubtotal().toFixed(2);
        if (discountEl) discountEl.textContent = '$' + getDiscount().toFixed(2);
        if (totalEl) totalEl.textContent = '$' + total.toFixed(2);
        var totalUsdEl = document.getElementById('pos-total-usd');
        if (totalUsdEl) totalUsdEl.textContent = 'US$' + (total / tipoCambio).toFixed(2);
        var paymentLabel = document.getElementById('pos-payment-label');
        if (paymentLabel) paymentLabel.textContent = moneda === 'USD' ? 'Pago USD' : 'Pago MXN';

        document.getElementById('pos-change').textContent =
            cambio >= 0
                ? (moneda === 'USD'
                    ? '$' + cambio.toFixed(2) + ' MXN / US$' + (cambio / tipoCambio).toFixed(2)
                    : '$' + cambio.toFixed(2) + ' MXN')
                : '0.00';

        // 🔥 MOSTRAR IVA
        var ivaEl = document.getElementById('pos-iva');
        if (ivaEl) {
            ivaEl.textContent = "$" + iva.toFixed(2);
        }

        // 🔥 TOTAL CON IVA
        var totalFinalEl = document.getElementById('pos-total-final');
        if (totalFinalEl) {
            totalFinalEl.textContent = "$" + totalFinal.toFixed(2);
        }
    }

    function clearCart() {
        cart = [];
        lastSale = null;
        document.getElementById('pos-payment').value = '';
        document.getElementById('pos-discount-type').value = 'amount';
        document.getElementById('pos-discount-value').value = '';
        document.getElementById('pos-moneda').value = 'MXN';
        document.getElementById('pos-change').textContent = '0.00';
        document.getElementById('pos-print-btn').style.display = 'none';
        renderCart();
    }

    // --- Register the sale via fetch ---
    function registerSale() {
        var tipoPagoEl = document.getElementById('pos-tipo-pago');
        var tipoPago = tipoPagoEl ? tipoPagoEl.value : 'efectivo';
        var discountTypeEl = document.getElementById('pos-discount-type');
        var discountValueEl = document.getElementById('pos-discount-value');
        var discountType = discountTypeEl ? discountTypeEl.value : 'amount';
        var discountValue = discountValueEl ? (parseFloat(discountValueEl.value) || 0) : 0;
        var moneda = getCurrency();
        if (cart.length === 0) return;

        var subtotal = getSubtotal();
        var discount = getDiscount();
        var total = getTotal();
        var pagoOriginal = getPaymentAmount();
        var pago = getPaymentInMXN();
        if (pago < total) {
            alert('El pago debe ser mayor o igual al total.');
            return;
        }

        // Read the CSRF token injected by anti-forgery-field
        var csrfInput = document.querySelector('input[name="__anti-forgery-token"]');
        var csrfToken = csrfInput ? csrfInput.value : '';

        var payload = {
            cotizacion_id: quoteId,
            items: cart.map(function (item) {
                return {
                    producto_id: item.producto_id,
                    nombre: item.nombre,
                    cantidad: item.cantidad,
                    precio: item.precio,
                    categoria: item.categoria || ''
                };
            }),
            pago: pago,
            pago_original: pagoOriginal,
            tipo_pago: tipoPago,
            moneda: moneda,
            tipo_cambio: tipoCambio,
            descuento_tipo: discountType,
            descuento_valor: discountValue
        };

        var btn = document.getElementById('pos-register-btn');
        btn.disabled = true;
        btn.textContent = 'Registrando...';

        fetch('/api/pos/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-Token': csrfToken,
                'x-requested-with': 'XMLHttpRequest'
            },
            body: JSON.stringify(payload)
        })
            .then(function (resp) { return resp.json(); })
            .then(function (data) {
                if (data.ok) {
                    lastSale = {
                        venta_id: data.venta_id, items: cart.slice(),
                        site_name: data.site_name,
                        company_name: data.company_name,
                        company_address: data.company_address,
                        total: data.total, pago: pago,
                        subtotal: data.subtotal || subtotal,
                        descuento: data.descuento || discount,
                        descuento_tipo: discountType,
                        descuento_valor: discountValue,
                        moneda: moneda,
                        tipo_cambio: tipoCambio,
                        pago_original: pagoOriginal,
                        cambio: data.cambio,
                        fecha: new Date().toLocaleString()
                    };
                    alert('Venta registrada #' + data.venta_id);
                    document.getElementById('pos-print-btn').style.display = '';
                    cart = [];
                    document.getElementById('pos-payment').value = '';
                    document.getElementById('pos-discount-type').value = 'amount';
                    document.getElementById('pos-discount-value').value = '';
                    document.getElementById('pos-moneda').value = 'MXN';
                    renderCart();
                } else {
                    alert('Error: ' + (data.error || 'Error desconocido'));
                }
            })
            .catch(function (err) {
                alert('Error de red: ' + err.message);
            })
            .finally(function () {
                btn.disabled = false;
                btn.textContent = 'Registrar Venta';
            });
    }

    // --- Print a receipt in a new window ---
    function renderTicketHTML(s) {

        var total = Number(s.total || 0);
        var tasaIva = 0.08;
        var subtotal = +(total / (1 + tasaIva)).toFixed(2);
        var iva = +(total - subtotal).toFixed(2);
        var descuento = Number(s.descuento || 0);
        var companyName = escapeHtml(s.company_name || s.site_name || 'POS');
        var companyAddress = escapeHtml(s.company_address || '');

        var rows = s.items.map(function (it) {
            var lineTotal = it.cantidad * it.precio;
            var lineSubtotal = lineTotal / (1 + tasaIva);

            return `
        <tr>
            <td>${it.nombre}</td>
            <td style="text-align:center">
                ${it.cantidad}
            </td>
            <td style="text-align:right">
                $${lineSubtotal.toFixed(2)}
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

            <img src="/images/logo.png?v=20260529" style="width:120px;height:auto;margin-bottom:10px;" alt="Logo">

            <h2>${companyName}</h2>

            ${companyAddress ? `<div style="font-size:11px;line-height:1.35;margin-bottom:6px;">${companyAddress}</div>` : ''}

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
                    <th>Subtotal</th>
                </tr>

            </thead>

            <tbody>

                ${rows}

            </tbody>

        </table>

        <div class="line"></div>

        <table>

            ${descuento > 0 ? `
            <tr>
                <td>Descuento:</td>
                <td style="text-align:right">-$${descuento.toFixed(2)}</td>
            </tr>` : ''}

            <tr>
                <td>Subtotal:</td>
                <td style="text-align:right">$${subtotal.toFixed(2)}</td>
            </tr>
            <tr>
                <td>IVA (8%):</td>
                <td style="text-align:right">$${iva.toFixed(2)}</td>
            </tr>
            <tr>
                <td><strong>Total:</strong></td>
                <td style="text-align:right">
                    <strong>
                        $${total.toFixed(2)}
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
    function printReceipt() {
        if (!lastSale) {
            alert('No hay venta para imprimir.');
            return;
        }

        var s = lastSale;

        var total = Number(s.total || 0);
        var descuento = Number(s.descuento || 0);
        var moneda = s.moneda || 'MXN';
        var tipoCambioTicket = Number(s.tipo_cambio || tipoCambio || 1);
        var pagoOriginal = Number(s.pago_original || 0);
        var tasaIva = 0.08;
        var subtotal = +(total / (1 + tasaIva)).toFixed(2);
        var iva = +(total - subtotal).toFixed(2);
        var companyName = escapeHtml(s.company_name || s.site_name || 'BAZAR DURAN');
        var companyAddress = escapeHtml(s.company_address || '');


        var w = window.open('', '_blank', 'width=400,height=600');
        if (!w) {
            alert('El bloqueador de ventanas emergentes impidió abrir el recibo.');
            return;
        }

        var rows = s.items.map(function (it) {
            var lineTotal = it.cantidad * it.precio;
            var lineSubtotal = lineTotal / (1 + tasaIva);

            return '<tr>'
                + '<td>' + escapeHtml(it.nombre) + '</td>'
                + '<td style="text-align:right">' + it.cantidad + '</td>'
                + '<td style="text-align:right">$' + lineSubtotal.toFixed(2) + '</td>'
                + '</tr>';
        }).join('');

        var html = `
    <html>
    <head>
        <title>Recibo #${s.venta_id}</title>
        <style>
            body {
                font-family: monospace;
                font-size: 12px;
                width: 300px;
                margin: 20px auto;
            }
            table { width: 100%; }
            .line { border-top: 1px dashed #000; margin: 8px 0; }
        </style>
    </head>
    <body>
        <div style="text-align:center">
            <img src="/images/logo.png?v=20260529" style="width:120px;height:auto;margin-bottom:10px;" alt="Logo">
            <h2 style="margin:0 0 4px;">${companyName}</h2>
            ${companyAddress ? `<div style="font-size:11px;line-height:1.35;margin-bottom:8px;">${companyAddress}</div>` : ''}
        </div>

        <p style="text-align:center">
            Venta #${s.venta_id}<br>
            ${s.fecha}
        </p>

        <div class="line"></div>

        <table>
            <tr>
                <th>Producto</th>
                <th>Cant</th>
                <th>Subtotal</th>
            </tr>
            ${rows}
        </table>

        <div class="line"></div>

        <table>
            ${descuento > 0 ? `
            <tr>
                <td>Descuento:</td>
                <td style="text-align:right">-$${descuento.toFixed(2)}</td>
            </tr>` : ''}
            <tr>
                <td>Subtotal:</td>
                <td style="text-align:right">$${subtotal.toFixed(2)}</td>
            </tr>
            <tr>
                <td>IVA (8%):</td>
                <td style="text-align:right">$${iva.toFixed(2)}</td>
            </tr>
            <tr>
                <td><strong>Total:</strong></td>
                <td style="text-align:right"><strong>$${total.toFixed(2)}</strong></td>
            </tr>
            <tr>
                <td>Pago:</td>
                <td style="text-align:right">${
                    moneda === 'USD'
                        ? 'US$' + pagoOriginal.toFixed(2) + ' ($' + s.pago.toFixed(2) + ' MXN)'
                        : '$' + s.pago.toFixed(2)
                }</td>
            </tr>
            ${moneda === 'USD' ? `
            <tr>
                <td>Tipo cambio:</td>
                <td style="text-align:right">$${tipoCambioTicket.toFixed(4)} MXN</td>
            </tr>` : ''}
            <tr>
                <td>Cambio:</td>
                <td style="text-align:right">$${s.cambio.toFixed(2)}</td>
            </tr>
        </table>

        <div class="line"></div>

        <p style="text-align:center">¡Gracias por su compra!</p>

    </body>
    </html>
    `;

        w.document.open();
        w.document.write(html);
        w.document.close();

        printWindowWhenReady(w);
    }

    function printWindowWhenReady(w) {
        var images = Array.prototype.slice.call(w.document.images || []);

        if (images.length === 0) {
            w.focus();
            w.print();
            return;
        }

        var remaining = images.length;
        var done = false;

        function printNow() {
            if (done) return;
            done = true;
            w.focus();
            w.print();
        }

        function imageDone() {
            remaining--;
            if (remaining <= 0) printNow();
        }

        images.forEach(function (img) {
            if (img.complete) {
                imageDone();
                return;
            }

            img.onload = imageDone;
            img.onerror = imageDone;
        });

        setTimeout(printNow, 1500);
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    return {
        addItem: addItem, removeItem: removeItem, updateQty: updateQty,
        clearCart: clearCart, calcChange: calcChange,
        registerSale: registerSale, printReceipt: printReceipt,
        addMiscCharge: addMiscCharge
    };



})();
