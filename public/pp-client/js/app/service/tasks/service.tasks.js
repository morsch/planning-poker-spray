var tasksService = angular.module('pp.service.tasks', ['pp.service.rest'], function () {
});

tasksService.constant('tasksConstants', {
    resource: 'tasks'
});

tasksService.factory('tasksService', ['$q', 'restService', 'tasksConstants', function ($q, restService, tasksConstants) {

    function Task(title, summary, team) {
        this.title = title;
        this.summary = summary;
        this.team = team;
    }

    return {
        getTasksByTeam: function (teamName) {

            var deferred = $q.defer();
            restService.findAll(tasksConstants.resource + '/' + teamName)
                .then(function (data) {
                    var tasks = [];
                    angular.forEach(data, function (value, key) {
                        tasks.push(new Task(value.title, value.summary, value.team));
                    });
                    deferred.resolve(tasks);
                }, function (error) {
                    deferred.reject(error);
                });
            return deferred.promise;
        }
    };
}]);