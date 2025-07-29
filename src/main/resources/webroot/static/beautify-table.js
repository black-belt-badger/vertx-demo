document.querySelectorAll('td.date').forEach(td => {
    const date = new Date(td.textContent);
    td.textContent = date.toLocaleDateString('en-US', {
        year: 'numeric', month: 'short', day: 'numeric'
      }
    );
  }
);
document.querySelectorAll('td.shares').forEach(td => {
    td.textContent = Number(td.textContent).toLocaleString();
  }
);

document.querySelectorAll('td.price').forEach(td => {
    const raw = td.textContent.trim();
    if (!raw) {
      td.textContent = '—';
      return;
    }
    if (raw.includes('-')) {
      const [from, to] = raw.split('-').map(s => parseFloat(s.trim()));
      if (!isNaN(from) && !isNaN(to)) {
        td.textContent = `$${from.toFixed(2)} - $${to.toFixed(2)}`;
      } else {
        td.textContent = '—';
      }
    } else {
      const value = parseFloat(raw);
      if (!isNaN(value)) {
        td.textContent = `$${value.toFixed(2)}`;
      } else {
        td.textContent = '—';
      }
    }
  }
);

document.querySelectorAll('td.large-number').forEach(td => {
    td.textContent = Number(td.textContent).toLocaleString();
  }
);
