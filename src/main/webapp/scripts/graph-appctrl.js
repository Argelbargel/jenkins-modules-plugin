angular.module('graphApp', [])
    .controller('GraphAppCtrl', ['$scope', '$http', '$timeout', function GraphAppCtrl($scope, $http, $timeout) {

        var graphDataModel = {nodes: [], connectors: []};
        var graphPlumb = jsPlumb.getInstance({Container: "graph"});
        var nodesSize = 0;
        var isBuilding = false;
        $scope.callAtTimeout = function () {
            $http.get(ajaxPath)
                .then(function (response) {
                        var data = response.data;
                        /* response.data can either be parsed JSON (object) or unparsed String() object
                         * (or plain type string if angular implementation changes). */
                        if (typeof data === 'string' || data instanceof String) {
                            data = JSON.parse(response.data);
                        }
                    var graph = JSON.parse(data.graph);
                    if (graph.isBuilding || nodesSize != graph.nodesSize || isBuilding != graph.isBuilding) {
                        nodesSize = graph.nodesSize;
                        isBuilding = graph.isBuilding;
                        $scope.graphViewModel = graph;
                            $timeout(function () {
                                graphPlumb.reset();
                                for (i = 0; i < graph.connectors.length; i++) {
                                    var connectorarrow = graph.connectors[i];
                                    graphPlumb.connect({
                                        source: connectorarrow.source,
                                        target: connectorarrow.target,
                                        overlays: [["Arrow", {
                                            location: 1,
                                            id: "arrow",
                                            length: 12,
                                            width: 12
                                        }]],
                                        anchors: [[1, 0, 1, 0, 0, 37], [0, 0, -1, 0, 0, 37]],
                                        connector: ["Flowchart", {stub: 25, gap: 0, midpoint: 0, alwaysRespectStubs: true}],
                                        endpoint: ["Blank", {}],
                                        paintStyle: {strokeStyle: 'grey', lineWidth: '3'}
                                    });
                                }
                                graphPlumb.repaintEverything();
                            }, 200);
                        }
                    }
                );

            $timeout(function () {
                $scope.callAtTimeout();
            }, 3000);
        };
        $scope.callAtTimeout();
    }])
    .directive('myGraph', function () {
        return {
            restrict: 'E',
            templateUrl: function (element, attrs) {
                return attrs.jenkinsurl + "/plugin/modules-plugin/scripts/graph-nodetemplate.html";
            },
            replace: true
        };
    })
    .filter('rawHtml', ['$sce', function ($sce) {
        return function (html) {
            return $sce.trustAsHtml(html);
        };
    }]);
