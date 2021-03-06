package net.somethingdreadful.MAL.api;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.somethingdreadful.MAL.BuildConfig;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.response.Activity;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.OAuth;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;

import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.ApacheClient;
import retrofit.converter.GsonConverter;

public class ALApi {
    // Anilist
    private static String anilistURL = "http://anilist.co/api";
    private static String accesToken;

    ALInterface service;

    public ALApi() {
        setupRESTService();
    }

    public static String getAnilistURL() {
        return anilistURL + "/auth/authorize?grant_type=authorization_code&client_id=" + BuildConfig.ANILIST_CLIENT_ID + "&redirect_uri=" + BuildConfig.ANILIST_CLIENT_REDIRECT_URI + "&response_type=code";
    }

    public static String getCode(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri.getQueryParameter("code");
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void setupRESTService() {
        if (accesToken == null && AccountService.getAccount() != null)
            accesToken = AccountService.getAccesToken();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setVersion(2)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new ApacheClient(new DefaultHttpClient()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Authorization", "Bearer " + accesToken);
                    }
                })
                .setEndpoint(anilistURL)
                .setConverter(new GsonConverter(gson))
                .build();
        service = restAdapter.create(ALInterface.class);
    }

    public OAuth getAuthCode(String code) {
        OAuth auth = service.getAuthCode("authorization_code", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, BuildConfig.ANILIST_CLIENT_REDIRECT_URI, code);
        accesToken = auth.access_token;
        setupRESTService();
        return auth;
    }

    public ArrayList<Activity> getActivity(String username) {
        return service.getActivity(username);
    }

    public Profile getCurrentUser() {
        return service.getCurrentUser();
    }

    public AnimeList getAnimeList(String username) {
        return service.getAnimeList(username);
    }

    public MangaList getMangaList(String username) {
        return service.getMangaList(username);
    }

    public void getAccesToken() {
        OAuth auth = service.getAccesToken("refresh_token", BuildConfig.ANILIST_CLIENT_ID, BuildConfig.ANILIST_CLIENT_SECRET, AccountService.getRefreshToken());
        accesToken = AccountService.setAccesToken(auth.access_token, Long.parseLong(auth.expires_in));
        setupRESTService();
    }

    public Anime getAnime(int id) {
        return service.getAnime(id);
    }

    public Manga getManga(int id) {
        return service.getManga(id);
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        return AnimeList.getData(service.searchAnime(query, page));
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        return MangaList.getData(service.searchManga(query, page));
    }

    public ArrayList<Manga> getUpcomingManga(int page) {
        return service.getUpcomingManga(page).getData();
    }

    public ArrayList<Anime> getUpcomingAnime(int page) {
        return service.getUpcomingAnime(page).getData();
    }

    public ArrayList<Manga> getJustAddedManga(int page) {
        return service.getJustAddedManga(page).getData();
    }

    public ArrayList<Anime> getJustAddedAnime(int page) {
        return service.getJustAddedAnime(page).getData();
    }

    public ArrayList<Anime> getAiringAnime(int page) {
        return service.getAiringAnime(page).getData();
    }

    public ArrayList<Manga> getAiringManga(int page) {
        return service.getAiringManga(page).getData();
    }

    public ArrayList<Anime> getYearAnime(int year, int page) {
        return service.getYearAnime(year, page).getData();
    }

    public ArrayList<Manga> getYearManga(int year, int page) {
        return service.getYearManga(year, page).getData();
    }

    public ArrayList<User> getFollowers(String user) {
        return service.getFollowers(user);
    }

    public boolean addOrUpdateAnime(Anime anime) {
        boolean result;
        if (anime.getCreateFlag())
            result = service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getPersonalComments(), anime.getRewatchCount()).getStatus() == 200;
        else
            result = service.updateAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore(), anime.getPersonalComments(), anime.getRewatchCount()).getStatus() == 200;
        return result;
    }

    public boolean deleteAnimeFromList(int id) {
        return service.deleteAnime(id).getStatus() == 200;
    }

    public boolean deleteMangaFromList(int id) {
        return service.deleteManga(id).getStatus() == 200;
    }

    public boolean addOrUpdateManga(Manga manga) {
        boolean result;
        if (manga.getCreateFlag())
            result = service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        else
            result = service.updateManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        return result;
    }

    public Profile getProfile(String name) {
        return service.getProfile(name);
    }
}
