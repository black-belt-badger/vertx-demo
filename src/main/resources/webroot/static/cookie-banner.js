document.addEventListener('DOMContentLoaded', function () {
    const banner = document.getElementById('cookie-banner');
    const acceptBtn = document.getElementById('accept-cookies');
    if (!localStorage.getItem('cookiesAccepted')) {
      banner.style.display = 'block';
    }
    acceptBtn.addEventListener('click', function () {
        localStorage.setItem('cookiesAccepted', 'true');
        banner.style.display = 'none';
      }
    );
  }
);
