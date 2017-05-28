angular.module('graphApp', [])
    .controller('GraphAppCtrl', ['$scope', '$http', '$timeout', function GraphAppCtrl($scope, $http, $timeout) {

        var graphDataModel = {columns: [], connectors: []};
        var graphPlumb = jsPlumb.getInstance({Container: "connectors"});
        var columnsSize = 0;
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
                    if (graph.isBuilding || columnsSize != graph.columns.length || isBuilding != graph.isBuilding) {
                        columnsSize = graph.columns.length;
                        isBuilding = graph.isBuilding;
                        $scope.graphViewModel = graph;
                        $scope.resURL = resURL;
                        $scope.rootURL = data.rootUrl;
                            $timeout(function () {
                                graphPlumb.reset();
                                for (var i = 0; i < graph.connectors.length; i++) {
                                    var connector = graph.connectors[i];
                                    graphPlumb.connect({
                                        source: connector.source,
                                        target: connector.target,
                                        overlays: [["Arrow", {
                                            location: 1,
                                            id: "arrow",
                                            length: 12,
                                            width: 12
                                        }]],
                                        anchors: [[1, 0.6, 1, 0], [0, 0.6, -1, 0]],
                                        connector: ["Flowchart", {
                                            stub: 25,
                                            gap: 0,
                                            midpoint: 0.95,
                                            alwaysRespectStubs: false
                                        }],
                                        endpoint: ["Blank", {}],
                                        paintStyle: {strokeStyle: 'grey', lineWidth: '3'},
                                        hoverPaintStyle: {strokeStyle: "red", lineWidth: '5'}
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
                return attrs.template;
            },
            replace: true
        };
    })
    .filter('rawHtml', ['$sce', function ($sce) {
        return function (html) {
            return $sce.trustAsHtml(html);
        };
    }]);
