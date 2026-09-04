(() => {
  'use strict';

  const currentPath = window.location.pathname.replace(/\/$/, '') || '/';
  document.querySelectorAll('[data-ais-route]').forEach((link) => {
    const route = link.getAttribute('data-ais-route');
    if (route && (currentPath === route || currentPath.startsWith(`${route}/`))) {
      link.setAttribute('aria-current', 'page');
    }
  });

  const navigation = document.getElementById('ais-primary-navigation');
  if (navigation && window.bootstrap?.Collapse) {
    navigation.querySelectorAll('a').forEach((link) => {
      link.addEventListener('click', () => {
        if (window.matchMedia('(max-width: 991.98px)').matches) {
          window.bootstrap.Collapse.getOrCreateInstance(navigation).hide();
        }
      });
    });
  }
})();
