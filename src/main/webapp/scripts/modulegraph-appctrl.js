angular.module('moduleGraphApp', [])
    .controller('ModuleGraphAppCtrl', ['$scope', '$http', '$timeout', function ModuleGraphAppCtrl($scope, $http, $timeout) {

        var moduleGraphDataModel = {nodes: [], connectors: []};
        var moduleGraphPlumb = jsPlumb.getInstance({Container: "modulegraph"});
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
                        var moduleGraph = JSON.parse(data.moduleGraph);
                        if (moduleGraph.isBuilding || nodesSize != moduleGraph.nodesSize || isBuilding != moduleGraph.isBuilding) {
                            nodesSize = moduleGraph.nodesSize;
                            isBuilding = moduleGraph.isBuilding;
                            $scope.moduleGraphViewModel = moduleGraph;
                            $timeout(function () {
                                moduleGraphPlumb.reset();
                                for (i = 0; i < moduleGraph.connectors.length; i++) {
                                    var connectorarrow = moduleGraph.connectors[i];
                                    moduleGraphPlumb.connect({
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
                                moduleGraphPlumb.repaintEverything();
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
    .directive('myModuleGraph', function () {
        return {
            restrict: 'E',
            templateUrl: function (element, attrs) {
                return attrs.jenkinsurl + "/plugin/modules/scripts/modulegraph-nodetemplate.html";
            },
            replace: true
        };
    })
    .filter('rawHtml', ['$sce', function ($sce) {
        return function (html) {
            return $sce.trustAsHtml(html);
        };
    }]);
