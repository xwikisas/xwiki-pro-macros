require(['jquery', new XWiki.Document('IconPicker', 'IconThemesCode').getURL('jsx')], function($) {
  const globalConfig = {
    'prefix': ''
  };

  const attachPickers = function(event, data) {
    let container = $((data && data.elements) || document);
    container.find('.xwiki-icon-picker').each(function () {
      let config = $.extend({}, globalConfig, $(this).data('config') || {});
      $(this).xwikiIconPicker(config);
    });
  };

  $(document).on('xwiki:dom:updated', attachPickers);
  attachPickers();
});