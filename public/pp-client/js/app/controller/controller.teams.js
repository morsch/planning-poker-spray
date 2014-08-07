var teams = angular.module('pp.controller.teams', ['pp.service.teams', 'pp.service.tasks'], function () {
});

teams.controller('TeamsController', ['$rootScope', '$scope', '$state', '$log', '$timeout', 'teamsService', 'tasksService',
    function ($rootScope, $scope, $state, $log, $timeout, teamsService, tasksService) {
        teamsService.getTeams()
            .then(function (data) {

                angular.forEach(data, function (value, key) {
                    tasksService.getTasksByTeam(value.name).then(function (data) {
                        if (data.length > 0)
                            value.count = data.length;
                        else
                            value.count = "\xA0";
                    });
                });

                $scope.teams = data;
            }, function () {
                $log.debug('teamService failed, no data returned.');
            });

        $scope.chooseTeam = function (teamName) {
            if ($rootScope.username != undefined && $rootScope.username.length > 0) {
                $rootScope.resetErrorMessage();
                $rootScope.setTeam(teamName);
                localStorage.username = $rootScope.username;
                $rootScope.state('tasks', {teamName: teamName});
            } else {
                $rootScope.showErrorMessage('Please choose a username');
            }
        };

    }]);
