var POS = (function () {
    'use strict';

    var cart = [];
    var lastSale = null;
    var allProducts = [];

    // Read products embedded as JSON in the data attribute
    function getProductData() {
        var el = document.getElementById('pos-app');
        if (!el) return [];
        try { return JSON.parse(el.getAttribute('data-productos') || '[]'); }
        catch (e) { return []; }
    }

    function init() {
        allProducts = getProductData();
        // Live search: show/hide cards as the user types
        var input = document.getElementById('pos-search');
        if (input) {
            input.addEventListener('input', function () {
                var term = this.value.toLowerCase().trim();
                document.querySelectorAll('.pos-product-card').forEach(function (card) {
                    var name = (card.getAttribute('data-nombre') || '').toLowerCase();
                    card.classList.toggle('hidden', !(!term || name.indexOf(term) !== -1));
                });
            });
        }
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
                nombre:      product.nombre,
                precio:      parseFloat(product.precio) || 0,
                cantidad:    1,
                stock:       parseInt(product.stock) || 0
            });
        }
        renderCart();
    }

    function removeItem(productId) {
        var idx = findCartItem(productId);
        if (idx >= 0) { cart.splice(idx, 1); renderCart(); }
    }

    function updateQty(productId, delta) {
        var idx = findCartItem(productId);
        if (idx < 0) return;
        cart[idx].cantidad += delta;
        if (cart[idx].cantidad <= 0) cart.splice(idx, 1);
        renderCart();
    }

    function getTotal() {
        return cart.reduce(function (sum, item) {
            return sum + item.cantidad * item.precio;
        }, 0);
    }

    // --- Render the cart panel ---
    function renderCart() {
        var container  = document.getElementById('pos-cart-items');
        var totalEl    = document.getElementById('pos-total');
        var registerBtn = document.getElementById('pos-register-btn');

        if (cart.length === 0) {
            container.innerHTML = '<p class="text-muted text-center">Carrito vacío</p>';
            totalEl.textContent = '$0.00';
            registerBtn.disabled = true;
            calcChange();
            return;
        }

        var html = '';
        cart.forEach(function (item) {
            var subtotal = (item.cantidad * item.precio).toFixed(2);
            html += '<div class="pos-cart-row">';
            html += '<span class="pos-cart-name">' + escapeHtml(item.nombre) + '</span>';
            html += '<span class="pos-cart-qty">';
            html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                  + ' onclick="POS.updateQty(' + item.producto_id + ', -1)">-</button>';
            html += '<strong class="mx-1">' + item.cantidad + '</strong>';
            html += '<button class="btn btn-sm btn-outline-secondary pos-qty-btn"'
                  + ' onclick="POS.updateQty(' + item.producto_id + ', 1)">+</button>';
            html += '</span>';
            html += '<span class="pos-cart-price">$' + subtotal + '</span>';
            html += '<span class="pos-cart-remove"'
                  + ' onclick="POS.removeItem(' + item.producto_id + ')">'
                  + '<i class="bi bi-x-circle"></i></span>';
            html += '</div>';
        });

        container.innerHTML = html;
        totalEl.textContent = '$' + getTotal().toFixed(2);
        registerBtn.disabled = false;
        calcChange();
    }

    function calcChange() {
        var total  = getTotal();
        var pago   = parseFloat(document.getElementById('pos-payment').value) || 0;
        var cambio = pago - total;
        document.getElementById('pos-change').textContent =
            cambio >= 0 ? cambio.toFixed(2) : '0.00';
    }

    function clearCart() {
        cart = [];
        lastSale = null;
        document.getElementById('pos-payment').value = '';
        document.getElementById('pos-change').textContent = '0.00';
        document.getElementById('pos-print-btn').style.display = 'none';
        renderCart();
    }

    // --- Register the sale via fetch ---
    function registerSale() {
        if (cart.length === 0) return;

        var total = getTotal();
        var pago  = parseFloat(document.getElementById('pos-payment').value) || 0;
        if (pago < total) {
            alert('El pago debe ser mayor o igual al total.');
            return;
        }

        // Read the CSRF token injected by anti-forgery-field
        var csrfInput = document.querySelector('input[name="__anti-forgery-token"]');
        var csrfToken = csrfInput ? csrfInput.value : '';

        var payload = {
            items: cart.map(function (item) {
                return { producto_id: item.producto_id,
                         cantidad:    item.cantidad,
                         precio:      item.precio };
            }),
            pago: pago
        };

        var btn = document.getElementById('pos-register-btn');
        btn.disabled    = true;
        btn.textContent = 'Registrando...';

        fetch('/api/pos/register', {
            method:  'POST',
            headers: {
                'Content-Type':     'application/json',
                'X-CSRF-Token':     csrfToken,
                'x-requested-with': 'XMLHttpRequest'
            },
            body: JSON.stringify(payload)
        })
        .then(function (resp) { return resp.json(); })
        .then(function (data) {
            if (data.ok) {
                lastSale = { venta_id: data.venta_id, items: cart.slice(),
                             site_name: data.company,
                             total: data.total, pago: pago,
                             cambio: data.cambio,
                             fecha: new Date().toLocaleString() };
                alert('Venta registrada #' + data.venta_id);
                document.getElementById('pos-print-btn').style.display = '';
                cart = [];
                document.getElementById('pos-payment').value = '';
                renderCart();
            } else {
                alert('Error: ' + (data.error || 'Error desconocido'));
            }
        })
        .catch(function (err) {
            alert('Error de red: ' + err.message);
        })
        .finally(function () {
            btn.disabled    = false;
            btn.textContent = 'Registrar Venta';
        });
    }

    // --- Print a receipt in a new window ---
    function printReceipt() {
        if (!lastSale) { alert('No hay venta para imprimir.'); return; }
        var s = lastSale;
        var w = window.open('', '_blank', 'width=400,height=600');
        if (!w) { alert('El bloqueador de ventanas emergentes impidió abrir el recibo.'); return; }
        var rows = s.items.map(function (it) {
            return '<tr><td>' + escapeHtml(it.nombre) + '</td>'
                 + '<td style="text-align:right">' + it.cantidad + '</td>'
                 + '<td style="text-align:right">$' + (it.cantidad * it.precio).toFixed(2) + '</td></tr>';
        }).join('');
        w.document.write(
            '<html><head><title>Recibo #' + s.venta_id + '</title>'
          + '<style>body{font-family:monospace;font-size:12px;width:300px;margin:20px auto}'
          + 'table{width:100%}.line{border-top:1px dashed #000;margin:8px 0}</style></head><body>'
          + '<h2 style="text-align:center">'+ s.site_name + '</h2>'
          + '<h3 style="text-align:center">Recibo de Venta</h3>'
          + '<p style="text-align:center">Venta #' + s.venta_id + '<br>' + s.fecha + '</p>'
          + '<div class="line"></div>'
          + '<table><tr><th>Producto</th><th>Cant</th><th>Total</th></tr>' + rows + '</table>'
          + '<div class="line"></div>'
          + '<table>'
          + '<tr><td>Total:</td><td style="text-align:right">$' + s.total.toFixed(2) + '</td></tr>'
          + '<tr><td>Pago:</td><td style="text-align:right">$' + s.pago.toFixed(2) + '</td></tr>'
          + '<tr><td>Cambio:</td><td style="text-align:right">$' + s.cambio.toFixed(2) + '</td></tr>'
          + '</table>'
          + '<div class="line"></div>'
          + '<p style="text-align:center">¡Gracias por su compra!</p>'
          + '</body></html>'
        );
        w.document.close();
        w.focus();
        w.print();
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

    return { addItem: addItem, removeItem: removeItem, updateQty: updateQty,
             clearCart: clearCart, calcChange: calcChange,
             registerSale: registerSale, printReceipt: printReceipt };
})();
