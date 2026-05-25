window.Caja = (() => {
    function getCsrfToken() {
        const input = document.querySelector('input[name="__anti-forgery-token"]');
        return input ? input.value : '';
    }

    async function guardarMovimiento() {
        const fecha = document.getElementById('fecha').value;
        const tipo = document.getElementById('tipo_movimiento').value;
        const monto = parseFloat(document.getElementById('monto').value || 0);
        const descripcion = document.getElementById('descripcion').value;

        if (!fecha) {
            alert('Seleccione la fecha');
            return;
        }
        if (!tipo) {
            alert('Seleccione el tipo de movimiento');
            return;
        }
        if (monto === 0) {
            alert('Capture el monto');
            return;
        }

        const payload = {
            fecha: fecha,
            tipo_movimiento: tipo,
            monto: monto,
            descripcion: descripcion
        };

        try {
            const response = await fetch('/api/caja/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-Token': getCsrfToken(),
                    'x-requested-with': 'XMLHttpRequest'
                },
                body: JSON.stringify(payload)
            });
            const data = await response.json();
            if (data.ok) {
                alert('Movimiento guardado correctamente');
                window.location.reload();
            } else {
                alert(data.error || 'Error al guardar');
            }
        } catch (err) {
            console.error(err);
            alert('Error de conexión');
        }
    }

    return {
        guardarMovimiento
    };
})();
