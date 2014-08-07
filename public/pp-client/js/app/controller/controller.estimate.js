var estimate = angular.module('pp.controller.estimate', ['pp.service.estimates', 'pp.service.tasks' ], function () {
});

estimate.controller('EstimateController', ['$rootScope', '$scope', '$stateParams', 'estimatesService', 'tasksService', function ($rootScope, $scope, $stateParams, estimatesService, tasksService) {
    $scope.taskId = $stateParams.taskId;
    estimatesService.getEstimatesByTaskAndUser($stateParams.taskId, $rootScope.username).then(function (data) {
        for (estimate in data) {
            estimate.username = $rootScope.username;
        }
        $scope.estimates = data;
    });

    $scope.failure = function () {
        for (var i in $scope.estimates) {
            if (!$scope.estimates[i].amount) {
                return true;
            }
        }
        return false;
    };

    $scope.ready = function () {
        var failure = $scope.failure();

        if (!failure) {
            $rootScope.state('result', {taskId: $scope.taskId});
        } else {
            $rootScope.showErrorMessage('Not all estimations entered.');
        }

    };

    $scope.pushEstimate = function (estimate) {
        estimatesService.pushEstimate($scope.taskId, $rootScope.username, estimate);
    };

}]);