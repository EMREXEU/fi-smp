app = angular.module('smp', ['ngRoute']);

app.config(function ($routeProvider, $httpProvider, $locationProvider) {

    $routeProvider.
            when('/', {
                templateUrl: 'partials/select_ncp.html',
                controller: 'home'
            }).
            otherwise({
                redirectTo: '/'
            });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

});

app.controller('home', function ($scope, $http) {
    $http.get('/smp/api/emreg').success(function (data) {
        $scope.emreg = data;
    })

});



