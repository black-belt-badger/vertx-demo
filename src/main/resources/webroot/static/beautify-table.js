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
    td.textContent = `$${Number(td.textContent).toFixed(2)}`;
  }
);

document.querySelectorAll('td.large-number').forEach(td => {
    td.textContent = Number(td.textContent).toLocaleString();
  }
);
