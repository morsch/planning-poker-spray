var pp = angular.module('pp', [
        'pp.controller.teams', 'pp.controller.result', 'pp.controller.estimate', 'pp.controller.tasks',
        'ui.router', 'ngCookies'],
    function () {
    });

pp.constant('_', window._); // Allows lodash usage - see http://lodash.com/docs

pp.config(['$stateProvider','$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {

    var states = [];

    $urlRouterProvider.otherwise('/teams');

    states.push({
        name: 'teams',
        url: '/teams',
        templateUrl: 'template/teams.html',
        controller: 'TeamsController',
        custom: {}
    });

    states.push({
        name: 'tasks',
        url: '/tasks/:teamName',
        templateUrl: 'template/tasks.html',
        controller: 'TasksController',
        custom: {
            nav: 'teams'
        }
    });

    states.push({
        name: 'result',
        url: '/result/:taskId',
        templateUrl: 'template/result.html',
        controller: 'ResultController',
        custom: {
            nav: 'tasks',
            back: true
        }
    });

    states.push({
        name: 'estimate',
        url: '/estimate/:taskId',
        templateUrl: 'template/estimate.html',
        controller: 'EstimateController',
        custom: {
            nav: 'tasks'
        }
    });

    for (var stateIndex in states) {
        var state = states[stateIndex];
        $stateProvider.state(state);
    }
}]);

pp.controller('AppController', ['$window', '$timeout', '$interval', '$state', '$rootScope',
    function ($window, $timeout, $interval, $state, $rootScope) {

        $rootScope.navigationFunctions = {
            teams : function() {
                return {
                    state: 'teams'
                };
            },
            tasks : function() {
                return {
                    state: 'tasks',
                    args: {teamName: $rootScope.team}
                };
            }
        };

        $rootScope.backFn = function () {
            $window.history.back();
        };

        $rootScope.state = function (stateName, args) {
            $state.transitionTo(stateName, args);
        };

        $rootScope.$on('$stateChangeStart',
            function (event, toState, toParams, fromState, fromParams) {

                if ($rootScope.interval) {
                    $interval.cancel($rootScope.interval);
                    $rootScope.interval = undefined;
                }
            });

        $rootScope.$on('$stateChangeSuccess',
            function (event, toState, toParams, fromState, fromParams) {

                if (toState && toState.custom.nav) {
                    $rootScope.nav = $rootScope.navigationFunctions[toState.custom.nav]();
                } else {
                    $rootScope.nav = undefined;
                }

                if (toState && toState.custom.back) {
                    $rootScope.back = true;
                } else {
                    $rootScope.back = undefined;
                }
            });

        $rootScope.resetErrorMessage = function () {
            $rootScope.errorMessage = '';
        };
        $rootScope.showErrorMessage = function (errorMessage) {
            $rootScope.errorMessage = errorMessage;
            $timeout(function () {
                $rootScope.resetErrorMessage();
            }, 5000);
        };

        $rootScope.setUsername = function (username) {
            $rootScope.username = username;
        };

        $rootScope.setTeam = function(team) {
            localStorage.team = team;
            $rootScope.team = team;
        };

        $rootScope.resetErrorMessage();
        $rootScope.setUsername(localStorage.username ? localStorage.username : '');
        $rootScope.setTeam(localStorage.team ? localStorage.team : '');

        $rootScope.currentTime = new Date();
        $interval(function (){
            $rootScope.currentTime = new Date();
            }, 5000);
    }]);
