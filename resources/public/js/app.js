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

function showModal(content) {
  $("#info-modal").find('p').html(content);
  $("#info-modal").css('left', $(window).width() / 2 + 130);
  $("#info-modal").modal('show');
}

var messageDispatcher = {
  'game-started' : function(msg) {
    showModal("The game has started!"); 
  },
  'mafia-chosen' : function(msg) {
    if (msg.mafia) {
      showModal("You and " + msg.other_mafia + " <strong>ARE</strong> the mafia!");   
    } else {
      showModal("You are <strong>NOT</strong> the mafia!");   
    }
  },
  'player-eliminated' : function(msg) {
    if (msg.mafia) {
      showModal(msg.eliminated + "was eliminated! They <strong>WERE</strong> mafia!");
    } else {
      showModal(msg.eliminated + "was eliminated! They were a civilian!");
    }
  },
  'game-over' : function(msg) {
    if (msg.winner == 'mafia') {
      showModal("The Mafia won!");
    } else {
      showModal("The civilians won!");
    }
  },
}

ws.onmessage = function (evt) 
{ 
  var data = JSON.parse(evt.data);
  console.log("Received:", data);
  if (_.has(messageDispatcher, data.event)) {
    messageDispatcher[data.event](data);
  }
};

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
