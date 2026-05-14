const Corte = (() => {
  const fmt = new Intl.NumberFormat("es-MX", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  const today = () => new Date().toISOString().slice(0, 10);
  const money = (v) => fmt.format(Number(v || 0));
  const el = (id) => document.getElementById(id);

  const esc = (s) =>
    String(s ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");

  function summaryLine(label, value) {
    return `
      <div class="row-line">
        <span>${esc(label)}</span>
        <strong>${esc(value)}</strong>
      </div>`;
  }

function renderSummary(r = {}, desde = "", hasta = "") {

  return `

      <div class="corte-header">

        <div style="text-align:center;margin-bottom:3mm;">
          <img
            src="/uploads/bdlogo.png"
            style="
              max-width:55mm;
              max-height:18mm;
              object-fit:contain;
            ">
        </div>

        <h3>Resumen del corte del día</h3>

        <div class="meta">
          ${esc(desde)} al ${esc(hasta)}
        </div>

      </div>

      <div class="corte-summary">

        ${summaryLine("Ventas", r.cantidad_ventas ?? 0)}

        ${summaryLine(
          "Total vendido",
          `$${money(r.total_vendido)}`
        )}

      </div>`;
}
  function renderSale(v) {
    const items = (v.items || []).map((i) => `
      <tr>
        <td>
          <div>${esc(i.nombre || "")}</div>
        </td>
        <td>
          ${esc(i.cantidad || 0)} x $${money(i.precio_unitario)}
        </td>
        <td>
          $${money(i.subtotal)}
        </td>
      </tr>
    `).join("");

    return `
      <div class="corte-sale">
        <div class="sale-title">
          <span>Venta #${esc(v.id)}</span>
          <span>${esc(v.fecha || "")}</span>
        </div>

        <table class="corte-items">
          <thead>
            <tr>
              <th>Producto</th>
              <th>Cant</th>
              <th>Importe</th>
            </tr>
          </thead>
          <tbody>
            ${items || `<tr><td colspan="3">Sin detalle</td></tr>`}
          </tbody>
        </table>

        <div class="corte-total">
          <span>Total</span>
          <span>$${money(v.total)}</span>
        </div>
      </div>`;
  }

  async function buscar() {
    const desde = el("desde").value || today();
    const hasta = el("hasta").value || desde;

    const resp = await fetch(`/corte/data?desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}`);
    const data = await resp.json();

    const ventas = data.ventas || [];
    const resumen = data.resumen || {};

    if (!ventas.length) {
      el("corte-result").innerHTML = `
        <div class="corte-empty">
          ${renderSummary(resumen, desde, hasta)}
          <div class="alert alert-warning mt-3 mb-0">No hay ventas en ese rango.</div>
        </div>`;
      return;
    }

    const ventasHtml = ventas.map(renderSale).join("");

    el("corte-result").innerHTML = `
      <div class="corte-receipt">
        ${renderSummary(resumen, desde, hasta)}
        ${ventasHtml}
      </div>`;
  }

  function imprimir() {
    window.print();
  }

  async function pdf() {
    const target = document.querySelector(".corte-receipt");
    if (!target) return;

    const canvas = await html2canvas(target, {
      scale: 2,
      useCORS: true,
      backgroundColor: "#ffffff",
    });

    const imgData = canvas.toDataURL("image/png");
    const pdf = new jspdf.jsPDF("p", "mm", "a4");

    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = pageWidth - 20;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;

    let heightLeft = imgHeight;
    let position = 10;

    pdf.addImage(imgData, "PNG", 10, position, imgWidth, imgHeight);
    heightLeft -= pageHeight - 20;

    while (heightLeft > 0) {
      position = heightLeft - imgHeight + 10;
      pdf.addPage();
      pdf.addImage(imgData, "PNG", 10, position, imgWidth, imgHeight);
      heightLeft -= pageHeight - 20;
    }

    pdf.save(`corte_${el("desde").value || today()}_${el("hasta").value || today()}.pdf`);
  }

  document.addEventListener("DOMContentLoaded", () => {
    if (el("desde") && !el("desde").value) el("desde").value = today();
    if (el("hasta") && !el("hasta").value) el("hasta").value = today();
    buscar();
  });

  return {
    buscar,
    imprimir,
    pdf,
  };
})();