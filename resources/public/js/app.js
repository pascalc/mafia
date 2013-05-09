var ws = new WebSocket("ws://localhost:4714/game/1");

ws.onopen = function()
{
  // Web Socket is connected, send data using send()
  console.log("Socket opened!");
};

ws.onclose = function()
{ 
  // websocket is closed.
  console.log("Socket closed!");
};

function send(msg) {
  ws.send(msg);
}

function register_player(name) {
  send(JSON.stringify(
    {'type': 'register-player',
     'name': name}
  ));
}

function register_viewer() {
  send(JSON.stringify(
    {'type': 'register-viewer'}
  ));
}

ws.onmessage = function (evt) 
{ 
  console.log("Received:", evt.data);
};

function suspicionList() {
  var elements = $("#list .element");
  var sorted = _.sortBy(elements, function (el) {
    return $(el).position().top;
  });
  return _.map(sorted, function(el) {
    return $(el).find(".player").text();
  });
}

function onMoved(instance, event, pointer) {
  console.log("Your suspicions are now: ", suspicionList());
}

$(document).ready(function() {
  var list = $("#list").get(0);
  var pckry = new Packery(list, {
    // options
    itemSelector: '.element',
    gutter: 10,
  }); 

  var itemElems = pckry.getItemElements();
  // for each item...
  for ( var i=0, len = itemElems.length; i < len; i++ ) {
    var elem = itemElems[i];
    // make element draggable with Draggabilly
    var draggie = new Draggabilly( elem );
    // bind Draggabilly events to Packery
    pckry.bindDraggabillyEvents( draggie );
    // Listen to events ourselves
    draggie.on('dragEnd', onMoved);
  } 
});
