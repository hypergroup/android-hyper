package io.hypergroup.hyper.json;

public class JsonFiles {

    public static final class BASICS {
        public static final String EMPTY = "{}";
        public static final String NULL_COLLECTION = "{\"collection\":null}";
        public static final String NULL_HREF = "{\"href\":null}";
        public static final String EMPTY_HREF = "{\"href\":\"\"}";
    }

    public static final class ROOT {
        public static final String URL = "/api";
        public static final String GET = "{\"href\":\"/api\",\"current_user\":{\"href\":\"/api/users/1\"},\"users\":{\"href\":\"/api/users\",\"search\":{\"action\":\"/api/users\",\"method\":\"GET\",\"input\":{\"search\":{\"type\":\"text\",\"required\":false,\"value\":\"Matt\"},\"page\":{\"type\":\"number\",\"required\":false,\"value\":1},\"page_size\":{\"type\":\"number\",\"required\":false,\"value\":20},\"sort_key\":{\"type\":\"text\",\"required\":false,\"value\":\"first_name\"},\"sort_direction\":{\"type\":\"text\",\"required\":false,\"value\":\"asc\"}}}}}";
    }

    public static final class USERS {
        public static final String URL = "/api/users";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/users/1\"},{\"href\":\"/api/users/100\"},{\"href\":\"/api/users/234\"},{\"href\":\"/api/users/456\"},{\"href\":\"/api/users/132\"},{\"href\":\"/api/users/789\"},{\"href\":\"/api/users/1233\"},{\"href\":\"/api/users/1434\"},{\"href\":\"/api/users/3\"},{\"href\":\"/api/users/567\"},{\"href\":\"/api/users/1442\"},{\"href\":\"/api/users/678\"},{\"href\":\"/api/users/2342\"},{\"href\":\"/api/users/3462\"},{\"href\":\"/api/users/4574\"},{\"href\":\"/api/users/35675\"},{\"href\":\"/api/users/5684\"},{\"href\":\"/api/users/243\"},{\"href\":\"/api/users/4\"},{\"href\":\"/api/users/5678\"}],\"prev\":{\"href\":\"/api/users?search=matt&page=1&page_size=20&sort_key=name&sort_direction=asc\"},\"next\":{\"href\":\"/api/users?search=matt&page=3&page_size=10&sort_key=name&sort_direction=asc\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users?search=matt&page=2&page_size=10&sort_key=name&sort_direction=asc\"}";
    }

    public static final class USER {
        public static final String URL = "/api/users/<user_id>";
        public static final String GET = "{\"id\":1,\"first_name\":\"Matt\",\"last_name\":\"McMurry\",\"display_name\":\"Matt McMurry\",\"created_on\":\"2014-06-02\",\"public\":\"true\",\"avatar\":{\"default\":{\"url\":\"http://0.gravatar.com/avatar/40d7909c10c12eb67bf967d6431d4e97\"}},\"groups\":{\"href\":\"/api/users/1/groups\"},\"friends\":{\"href\":\"/api/users/1/friends\",\"count\":10},\"notes\":{\"href\":\"/api/users/1/notes\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users/1\"}";
    }

    public static final class USER_GROUPS {
        public static final String URL = "/api/users/<user_id>/groups";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/groups/1\"},{\"href\":\"/api/groups/100\"},{\"href\":\"/api/groups/234\"},{\"href\":\"/api/groups/456\"},{\"href\":\"/api/groups/132\"},{\"href\":\"/api/groups/789\"},{\"href\":\"/api/groups/1233\"},{\"href\":\"/api/groups/1434\"},{\"href\":\"/api/groups/3\"},{\"href\":\"/api/groups/567\"},{\"href\":\"/api/groups/1442\"},{\"href\":\"/api/groups/678\"},{\"href\":\"/api/groups/2342\"},{\"href\":\"/api/groups/3462\"},{\"href\":\"/api/groups/4574\"},{\"href\":\"/api/groups/35675\"},{\"href\":\"/api/groups/5684\"},{\"href\":\"/api/groups/243\"},{\"href\":\"/api/groups/4\"},{\"href\":\"/api/groups/5678\"}],\"prev\":{\"href\":\"/api/users/1/groups?page=1&page_size=20\"},\"next\":{\"href\":\"/api/users/1/groups?page=3&page_size=10\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users/1/groups?page=2&page_size=10\"}";
    }

    public static final class GROUPS {
        public static final String URL = "/api/groups";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/groups/1\"},{\"href\":\"/api/groups/100\"},{\"href\":\"/api/groups/234\"},{\"href\":\"/api/groups/456\"},{\"href\":\"/api/groups/132\"},{\"href\":\"/api/groups/789\"},{\"href\":\"/api/groups/1233\"},{\"href\":\"/api/groups/1434\"},{\"href\":\"/api/groups/3\"},{\"href\":\"/api/groups/567\"},{\"href\":\"/api/groups/1442\"},{\"href\":\"/api/groups/678\"},{\"href\":\"/api/groups/2342\"},{\"href\":\"/api/groups/3462\"},{\"href\":\"/api/groups/4574\"},{\"href\":\"/api/groups/35675\"},{\"href\":\"/api/groups/5684\"},{\"href\":\"/api/groups/243\"},{\"href\":\"/api/groups/4\"},{\"href\":\"/api/groups/5678\"}],\"prev\":{\"href\":\"/api/groups?search=matt&page=1&page_size=20\"},\"next\":{\"href\":\"/api/groups?search=matt&page=3&page_size=10\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/groups?search=matt&page=2&page_size=10\"}";
    }

    public static final class GROUP {
        public static final String URL = "/api/groups/<group_id>";
        public static final String GET = "{\"id\":400,\"name\":\"Aviation\",\"description\":\"This group is all about aviation\",\"created_on\":\"2014-06-02\",\"public\":\"true\",\"administrator\":{\"href\":\"/api/users/1\"},\"avatar\":{\"default\":{\"url\":\"http://www.halland.com/wp-content/uploads/2013/10/Aviation3.jpg\"}},\"readme\":{\"url\":\"http://public.ccsds.org/publications/archive/650x0b1.pdf\"},\"members\":{\"href\":\"/api/groups/400/users\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users/1\"}";
    }

    public static final class GROUP_MEMBERS {
        public static final String URL = "/api/groups/<group_id>/members";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/users/1\"},{\"href\":\"/api/users/100\"},{\"href\":\"/api/users/234\"},{\"href\":\"/api/users/456\"},{\"href\":\"/api/users/132\"},{\"href\":\"/api/users/789\"},{\"href\":\"/api/users/1233\"},{\"href\":\"/api/users/1434\"},{\"href\":\"/api/users/3\"},{\"href\":\"/api/users/567\"},{\"href\":\"/api/users/1442\"},{\"href\":\"/api/users/678\"},{\"href\":\"/api/users/2342\"},{\"href\":\"/api/users/3462\"},{\"href\":\"/api/users/4574\"},{\"href\":\"/api/users/35675\"},{\"href\":\"/api/users/5684\"},{\"href\":\"/api/users/243\"},{\"href\":\"/api/users/4\"},{\"href\":\"/api/users/5678\"}],\"prev\":{\"href\":\"/api/groups/20/members?page=1&page_size=20\"},\"next\":{\"href\":\"/api/groups/20/members?page=3&page_size=20\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/groups/20/members?page=2&page_size=20\"}";
    }

    public static final class FRIENDS {
        public static final String URL = "/api/users/<user_id>/friends";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/users/1\"},{\"href\":\"/api/users/100\"},{\"href\":\"/api/users/234\"},{\"href\":\"/api/users/456\"},{\"href\":\"/api/users/132\"},{\"href\":\"/api/users/789\"},{\"href\":\"/api/users/1233\"},{\"href\":\"/api/users/1434\"},{\"href\":\"/api/users/3\"},{\"href\":\"/api/users/567\"},{\"href\":\"/api/users/1442\"},{\"href\":\"/api/users/678\"},{\"href\":\"/api/users/2342\"},{\"href\":\"/api/users/3462\"},{\"href\":\"/api/users/4574\"},{\"href\":\"/api/users/35675\"},{\"href\":\"/api/users/5684\"},{\"href\":\"/api/users/243\"},{\"href\":\"/api/users/4\"},{\"href\":\"/api/users/5678\"}],\"prev\":{\"href\":\"/api/users/1/friends?page=1&page_size=20\"},\"next\":{\"href\":\"/api/users/1/friends?page=3&page_size=10\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users/1/friends?page=2&page_size=10\"}";
    }

    public static final class NOTES {
        public static final String URL = "/api/users/<user_id>/notes";
        public static final String GET = "{\"collection\":[{\"href\":\"/api/notes/1\"},{\"href\":\"/api/notes/100\"},{\"href\":\"/api/notes/234\"},{\"href\":\"/api/notes/456\"},{\"href\":\"/api/notes/132\"},{\"href\":\"/api/notes/789\"},{\"href\":\"/api/notes/1233\"},{\"href\":\"/api/notes/1434\"},{\"href\":\"/api/notes/3\"},{\"href\":\"/api/notes/567\"},{\"href\":\"/api/notes/1442\"},{\"href\":\"/api/notes/678\"},{\"href\":\"/api/notes/2342\"},{\"href\":\"/api/notes/3462\"},{\"href\":\"/api/notes/4574\"},{\"href\":\"/api/notes/35675\"},{\"href\":\"/api/notes/5684\"},{\"href\":\"/api/notes/243\"},{\"href\":\"/api/notes/4\"},{\"href\":\"/api/notes/5678\"}],\"prev\":{\"href\":\"/api/users/1/notes?page=1&page_size=20\"},\"next\":{\"href\":\"/api/users/1/notes?page=3&page_size=10\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/users/1/notes?page=2&page_size=10\"}";
    }

    public static final class NOTE {
        public static final String URL = "/api/notes/<note_id>";
        public static final String GET = "{\"id\":10001,\"title\":\"Awesome Note\",\"body\":\"This is the best note.\",\"created_on\":\"2014-06-02\",\"public\":\"true\",\"attachment\":{\"url\":\"http://www.google.com\"},\"root\":{\"href\":\"/api\"},\"href\":\"/api/notes/10001\"}";
    }
}
