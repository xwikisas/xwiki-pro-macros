require(['jquery'], function($) {

  let shouldAttachPicker = true;
  let pickerRequired = true;

  const attachPicker = function(config) {
    shouldAttachPicker = false;
    var iconOptions = {};
    iconOptions['prefix'] = config.prefix;
    $('.buttonIconPicker').xwikiIconPicker(iconOptions);
  };
  const requireIconPicker = function(config) {
    pickerRequired = false;
    require([config['icon-picker']], function () {
      attachPicker(config);
    });
  };



  let button = $('.buttonIconPicker');
  if (button.length > 0) {
    requireIconPicker(button.data('config'));
  }
  $('.macro-editor-modal').on('focus', '.buttonIconPicker', function() {
    if (pickerRequired) {
      requireIconPicker($('.buttonIconPicker').data('config'));
      $(this).focus();
    } else if (shouldAttachPicker) {
      attachPicker($('.buttonIconPicker').data('config'));
      $(this).focus();
    }
  });

  $('.macro-editor-modal').on('click', 'button', function () {
    shouldAttachPicker = true;
  });
});