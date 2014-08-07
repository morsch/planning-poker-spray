var tasks = angular.module('pp.controller.tasks', ['pp.service.tasks'], function () {
});

tasks.controller('TasksController', ['$scope', '$rootScope','$stateParams', 'tasksService', function ($scope, $rootScope, $stateParams, tasksService) {
    $scope.teamName = $stateParams.teamName;
    tasksService.getTasksByTeam($stateParams.teamName).then(function (data) {
        $scope.tasks = data;
    });
    $scope.chooseTask = function (taskId) {
        $rootScope.state('estimate', {taskId: taskId});
    };
}]);