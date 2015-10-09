app = angular.module('review', ['ngRoute', 'ngCookies']);

app.config(function ($routeProvider, $httpProvider, $locationProvider) {

    $routeProvider.
        when('/', {
            templateUrl: 'partials/reviewResults.html',
            controller: 'home'
        }).
        otherwise({
            redirectTo: '/'
        });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
});

app.controller('home', function ($scope, $http, $sce, $cookies, $timeout) {

   $scope.hello = 'jee!';

});



