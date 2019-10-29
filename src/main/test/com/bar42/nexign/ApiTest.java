package com.bar42.nexign;

import io.restassured.response.ValidatableResponse;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ApiTest extends Base
{
    @Test
    public void testCreate()
    {
        ValidatableResponse creationResponse = createUser("Created", "NewUser");
        // самое простое - проверка что код ошибки при создании пользователя = 200 (OK)
        creationResponse.statusCode(200);

        String creationText = creationResponse.extract().asString();
        // В [{ID=1... "ID" должно быть в кавычках: без них парсер JSON спотыкается об I.
        // Ах, если бы сервер возвращал валидный JSON - его бы можно было бы протестировать в rest assured,
        // а так - мы берём ответ от сервера в виде текста и проверяем наличие в нём ожидаемой строки.
        // Не писать же свой парсер для этого (а если и писать, то он всё равно не будет в контексте rest assured)
        assertContains(creationText, "FIRSTNAME=Created, LASTNAME=NewUser");
        
        // и не забыть проверить, что новый пользователь есть в списке всех пользователей:
        // он должен выглядеть так же, как и в сообщении о создании пользователя, только без "[]"
        assertContains(getUserList(), trimBrackets(creationText));
    }
    
    @Test
    public void testList()
    {
        // чтобы список пользователей не был пустым, мы добавим туда пользователя
        createUser("ReadThis", "UserInfo");
        
        // теперь мы можем прочитать список пользователей (он как минимум должен выполниться успещно)
        String listUsersResponse = listUsers().statusCode(200).extract().asString();
        // и проверить что наш пользователь там есть
        assertContains(listUsersResponse, "FIRSTNAME=ReadThis, LASTNAME=UserInfo");
    }
    
    @Test
    public void testRead()
    {
        // чтобы список пользователей не был пустым, мы добавим туда пользователя
        int id = getId(createUser("Single", "UserById"));
        
        // теперь мы можем прочитать информацию о пользователе (она как минимум должен прочитаться успешно)
        String usersInfoResponse = getUser(id).statusCode(200).extract().asString();
        // и проверить что наш пользователь там есть
        assertContains(usersInfoResponse, "FIRSTNAME=Single, LASTNAME=UserById");
    }
    
    @Test
    public void testUpdate()
    {
        // создадим пользователя, которого потом мы будеи менять
        int id = getId(createUser("UnUpdated", "OldInfo"));
        
        // теперь нам нужно мзиенить информацию о нём (и проверить, что запрос как минимум выполнился без ошибок)
        updateUser(id, "Updated", "NewInfo").statusCode(200);
        // теперь проверим инфломацию о нашем пользователе - там должно быть новое имя
        assertContains(getUser(id).extract().asString(), "FIRSTNAME=Updated, LASTNAME=NewInfo");
    }
    
    @Test
    public void testDelete()
    {
        // создадим пользователя, которого потом мы будеи удалять
        ValidatableResponse createdUser = createUser("ToDelete", "Victim");
        int id = getId(createdUser);
        String usersInfoResponse = getUser(id).extract().asString();
        // и проверить что наш пользователь реально создался, ведь
        // если пользователь не создался и отсутствует при удалении, то тест не найдёт эту ошибку
        assertContains(usersInfoResponse, "FIRSTNAME=ToDelete, LASTNAME=Victim");
    
        // теперь нам нужно удалить этого пользователя (и проверить, что запрос как минимум выполнился без ошибок)
        deleteUser(id).statusCode(200);
        
        // и проверить, что удалённого пользователя действительно нет
        String allUsers = getUserList();
        assertFalse(
            "\nExpected       "+allUsers+"\nnot to contain "+ trimBrackets(createdUser.extract().asString()),
            allUsers.contains(trimBrackets(createdUser.extract().asString()))
        );
    }
}
